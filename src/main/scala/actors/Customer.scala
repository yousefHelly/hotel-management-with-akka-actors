package actors

import akka.actor.{Actor, ActorRef}
import Customer._
import services.Customer.{GetCustomer, PostCustomer}
import models.{Customer => CustomerType}
import services.Reservation.GetCustomerReservation

import java.time.{LocalDate, LocalDateTime}
class Customer extends Actor{
  override def receive: Receive = {
    case initializeCustomer(name, contactNumber) =>
      GetCustomer(CustomerType(name = name, contactNumber = contactNumber)) match {
        case Some(customer) =>
          context.become(initializedCustomer(customer))
        case None =>
          PostCustomer(CustomerType(name = name, contactNumber = contactNumber)) match {
          case id =>
            println(s"[ $name ] Initialized as a customer successfully!")
            context.become(initializedCustomer(CustomerType(id, name, contactNumber)))
        }
      }
    case _ => println("Can't take any actions before initialization!")
  }
  // initialized behavior with customer's data
  private def initializedCustomer(customer: CustomerType): Receive = {
    case CheckIn(hotel, roomNumber) => hotel ! CheckInRequest(customer.id, roomNumber)
    case CheckOut(hotel, roomNumber) =>
      GetCustomerReservation(customer.id, roomNumber) match {
        case Some((customerId, roomId, startDate)) =>
          hotel ! CheckOutRequest(customerId, roomId, startDate, roomNumber)
        case None => println(s"[ ${self.path.name} ] no reservation found for Room-$roomNumber !")
      }
    case MakeReservation(hotel, roomNumber, startDate, endDate) =>
      hotel ! BookRoom(customer.id, roomNumber, startDate, endDate)
    case Maintenance(hotel, roomNumber, problem) => hotel ! MaintenanceRequest(customer.id, roomNumber, problem)
  }
}

object Customer {
  case class initializeCustomer(name: String, contactNumber: String)
  case class CheckIn(hotel: ActorRef,roomNumber: Int)
  case class CheckOut(hotel: ActorRef,roomNumber: Int)
  case class Maintenance(hotel: ActorRef, roomNumber: Int, problem: String)
  case class CheckInRequest(customerId: Int, roomNumber: Int)
  case class MaintenanceRequest(customerId: Int, roomNumber: Int, problem: String)
  case class CheckOutRequest(customerId: Int, roomId: Int, startDate: LocalDate, roomNumber: Int)
  case class MakeReservation(hotel: ActorRef,roomNumber: Int, startDate: LocalDate, endDate: LocalDate)
  case class BookRoom(customerId: Int, roomNumber: Int, startDate: LocalDate, endDate: LocalDate)
//  case class SendMaintainance(hotel: ActorRef, )
}
