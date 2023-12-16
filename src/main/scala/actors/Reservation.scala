package actors

import actors.Hotel.{CheckOutRoom, IsInitialized, ReserveRoom}
import actors.Reservation.{GenerateBill, SetRoomStatusTo, TellRoomToBeReserved}
import akka.actor.Actor
import models.{Reservation => ReservationType}
import services.Reservation._
import models.ReservationStatus
import models.RoomTypes
import services.Room.{GetReservedRoom, GetRoom, GetRoomByNumber, getRoomWithReservationCount}
import models.RoomStatus

import java.time.LocalDate
class Reservation extends Actor{
  override def receive: Receive = {
    case IsInitialized(id) =>
      InitializeReservation(id)
      self ! TellRoomToBeReserved

    case ReserveRoom(roomNumber, startDate, endDate, customerId) =>
      GetRoomByNumber(roomNumber) match {
        case room =>
          PostReservation(ReservationType(customerId = customerId, startDate = startDate, endDate = endDate, roomId = room.id, status = ReservationStatus.Active)) match {
            case reservationId if reservationId>0 =>
              println(s"[ ${self.path.name} ] reservation created successfully for customer id $customerId , Room-$roomNumber , starting from ${startDate.toString} to ${endDate.getOrElse("unknown end date")} , current reservation status is ${ReservationStatus.Active}")
              InitializeReservation(reservationId)
              self ! TellRoomToBeReserved
            case 0 =>  println(s"[ ${self.path.name} ] failed to Post the reservation")
            case -1 =>  println(s"[ ${self.path.name} ] Room-$roomNumber is Reserved")
          }
        case _ => println(s"[ ${self.path.name} ] failed to Get the roomId for a reservation")
      }
  }
  private def initializedReservation(reservation : ReservationType) : Receive = {
    case TellRoomToBeReserved =>
      GetReservedRoom(reservation.id, reservation.roomId) match {
        case Some(roomNumber) =>
          if (LocalDate.now().equals(reservation.startDate)) {
            context.system.actorSelection(s"/user/Hotel/Room-${roomNumber}") ! SetRoomStatusTo(RoomStatus.CheckedIn)
          } else {
            context.system.actorSelection(s"/user/Hotel/Room-${roomNumber}") ! SetRoomStatusTo(RoomStatus.Booked)
          }
        case None => println(s"[ ${self.path.name} ] Room with id ${reservation.roomId} is Currently available")
      }
    case CheckOutRoom =>{
      SetReservationCompleted(reservation.id, ReservationStatus.Completed, Some(LocalDate.now())) match {
        case num if num>0 =>
          getRoomWithReservationCount(reservation.roomId) match {
            case (room, reservationCount) =>
              if (reservationCount>0) {
                context.system.actorSelection(s"/user/Hotel/Room-${room.number}") ! SetRoomStatusTo(RoomStatus.Booked, true)
              } else {
                context.system.actorSelection(s"/user/Hotel/Room-${room.number}") ! SetRoomStatusTo(RoomStatus.Available, true)
              }
              val stayDuration = reservation.endDate.getOrElse(LocalDate.now()).until(reservation.startDate).getDays
              sender() ! GenerateBill(reservation.id, stayDuration, room.roomType)
            case _ => println(s"[ ${self.path.name} ] failed to update the room status after the reservation completed")
          }
        case _ => println(s"[ ${self.path.name} ] failed to update status of reservation to completed")
      }
    }
  }
  private def InitializeReservation = (id: Int)=> {
    GetReservation(id) match {
      case reservation =>
        context.become(initializedReservation(reservation))
        self ! TellRoomToBeReserved
      case _ => println(s"[ ${self.path.name} ] failed to Fetch the reservation")
    }
  }
}
object Reservation {
  case object TellRoomToBeReserved
  case class SetRoomStatusTo(status: RoomStatus.status, isCheckedOut: Boolean = false)
  case class GenerateBill(reservationId: Int, stayDuration: Double, roomType:RoomTypes.room)
}