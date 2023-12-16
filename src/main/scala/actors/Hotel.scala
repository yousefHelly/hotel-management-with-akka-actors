package actors

import actors.Hotel._
import models.RoomTypes
import models.{RoomStatus => Status}
import services.Room.GetAllRooms
import akka.actor.{Actor, Props}
import Room.RoomStatus
import actors.Customer.{BookRoom, CheckInRequest, CheckOutRequest, MaintenanceRequest}
import actors.Reservation.GenerateBill
import services.Reservation.{GetReservations, HasReservation}

import java.time.{LocalDate, LocalDateTime}
class Hotel() extends Actor{
  override def receive: Receive = InitialHotelReceive
  private def InitialHotelReceive :Receive = {
    case InitializeHotel(chargesForSingleRoom, chargesForDoubleRoom)=>
      GetAllRooms() match {
        case roomsIds =>  for {
          (id, i) <- roomsIds.zipWithIndex
        } yield { context.actorOf(Props[Room],s"Room-${i+1}") ! IsInitialized(id) }
          GetReservations() match {
            case reservations =>
              for{
                (id, cId, rNum, startDate) <- reservations
              } yield {
                context.actorOf(Props[Reservation],s"reservation-$cId-$rNum-${startDate.toString}") ! IsInitialized(id)
              }
          }
          context.become(HotelReceive((chargesForSingleRoom, chargesForDoubleRoom), roomsIds.length))
      }
    case _ => println(s"[ ${self.path.name} ] hotel hasn't been initialized yet!")
  }
  private def HotelReceive(charges: (Double, Double), rooms: Int): Receive =  {
      case AddRoom(roomType) =>
            val newRoom = context.actorOf(Props[Room], s"Room-${rooms + 1}")
            println(s"[ ${self.path.name} ] Room-${rooms + 1} created successfully!")
            newRoom ! InitializeRoom(rooms + 1, roomType, Status.Available)
            context.become(HotelReceive(charges, rooms + 1))
      case GetRoomStatus(number) =>
        context.child(s"Room-$number") match {
          case Some(room) => room ! WhatIsYourStatus
          case _ => println(s"[ ${self.path.name} ] Room-$number not found")
        }
      case RoomStatus(number, status) => println(s"[ ${self.path.name} ] room-$number is $status")
      case CheckInRequest(customerId, roomNumber) => context.child(s"Room-$roomNumber") match {
        case Some(room) =>
          context.child(s"reservation-$customerId-$roomNumber-${LocalDate.now().toString}") match {
            case Some(value) => println(s"[ ${self.path.name} ] the reservation for Room-$roomNumber by CustomerID:$customerId on date:${LocalDate.now().toString} has been created before")
            case None =>
              val reservation = context.actorOf(Props[Reservation], s"reservation-$customerId-$roomNumber-${LocalDate.now().toString}")
              reservation ! ReserveRoom(roomNumber, LocalDate.now(), None, customerId)
          }
        case _ => println(s"[ ${self.path.name} ] Room-$roomNumber not found")
      }
      case MaintenanceRequest(customerId, roomNumber, problem) => context.child(s"Room-$roomNumber") match {
        case Some(room) =>
          HasReservation(customerId, roomNumber) match {
            case num if num>0 =>
              println(s"[ ${self.path.name} ] Sending maintenance for Room-$roomNumber to solve problem : '$problem'")
            case _ => println(s"[ ${self.path.name} ] customer with id $customerId can't ask for maintenance for Room-$roomNumber without a reservation")
          }
        case _ => println(s"[ ${self.path.name} ] Room-$roomNumber not found")
      }
      case CheckOutRequest(customerId, roomId, startDate, roomNumber) => context.child(s"Room-$roomNumber") match {
        case Some(room) =>
          context.child(s"reservation-$customerId-$roomNumber-${startDate.toString}") match {
            case Some(reservation) => reservation ! CheckOutRoom
            case _ => println(s"[ ${self.path.name} ] reservation for Room-$roomNumber with customer id $customerId not found")
          }
        case _ => println(s"[ ${self.path.name} ] Room-$roomNumber not found")
      }
      case BookRoom(customerId, roomNumber, startDate, endDate) => context.child(s"Room-$roomNumber") match {
        case Some(room) =>
          context.child(s"reservation-$customerId-$roomNumber-${startDate.toString}") match {
            case Some(value) => println(s"[ ${self.path.name} ] the reservation for Room-$roomNumber by CustomerID:$customerId on date:${startDate.toString} has been created before")
            case None =>
              val reservation = context.actorOf(Props[Reservation], s"reservation-$customerId-$roomNumber-${startDate.toString}")
              reservation ! ReserveRoom(roomNumber, startDate, Some(endDate), customerId)
          }
        case _ => println(s"[ ${self.path.name} ] Room-$roomNumber not found")
      }
      case GenerateBill(reservationId, stayDuration, roomType) =>
        val durationWithMinimum = if(stayDuration.abs>0)stayDuration.abs else 1
        val totoalCost = if (roomType == RoomTypes.Single) durationWithMinimum * charges._1 else durationWithMinimum * charges._2
        println(s"[ ${self.path.name} ] the cost of reservation with id $reservationId =  ${durationWithMinimum.toInt} days X ${roomType}(${if (roomType == RoomTypes.Single) s"${charges._1}" else s"${charges._2}"}$$) = $totoalCost$$")
        val newBill = context.actorOf(Props[Billing], s"billing-$reservationId")
        newBill ! CreateBill(reservationId, totoalCost, LocalDate.now())

      case GenerateReport => context.child("billing-report") match {
        case Some(billingReport) => billingReport ! GenerateReport
        case None =>
          context.actorOf(Props[Billing], "billing-report") ! GenerateReport
      }

    }
}
object Hotel {
  case class InitializeHotel(chargesForSingleRoom: Double, chargesForDoubleRoom: Double)
  case class IsInitialized(id: Int)
  case class AddRoom(roomType: RoomTypes.room)
  case class InitializeRoom(number: Int, roomType: RoomTypes.room, roomStatus: Status.status)
  case class GetRoomStatus(number: Int)
  case class CheckInRoom(customerId: Int)
  case object CheckOutRoom
  case class ReserveRoom(roomNumber: Int, startDate: LocalDate, endDate: Option[LocalDate], customerId: Int)
  case object WhatIsYourStatus
  case class CreateBill(reservationId: Int, amount: Double, date: LocalDate)
  case object GenerateReport
}
