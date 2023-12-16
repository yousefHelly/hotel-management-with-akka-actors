package actors

import actors.Hotel.{CreateBill, GenerateReport}
import akka.actor.Actor
import models.{Billing => BillingType}
import services.Billing.{GetAllBills, PostBill}

class Billing extends Actor{
  override def receive: Receive = {
    case CreateBill(reservationId, amount, date) =>
      PostBill(BillingType(amount = amount, reservationId = reservationId, date = date)) match {
        case i if i > 0 =>
          println(s"[ ${self.path.parent.name} -> ${self.path.name} ] Bill has been created successfully for reservationID:${reservationId} with amount ${amount}$$ on ${date.toString()}")
        case _ =>
          println(s"[ ${self.path.parent.name} -> ${self.path.name} ] Failed to create a bill for reservationID:${reservationId}")
      }
    case GenerateReport =>
      GetAllBills() match {
        case (bills) =>
          for {
            (billId, amount, startDate, checkoutDate, roomNumber, roomType, customerName) <- bills
          } yield {
            println(s"[ ${self.path.parent.name} -> ${self.path.name} ] BillID:$billId , customer name:$customerName , amount:${amount}$$ , start date:$startDate , checkout date:$checkoutDate , room number:$roomNumber , room type:$roomType")
          }
        case _ => println(s"[ ${self.path.parent.name} -> ${self.path.name} ] Failed to make a report for current bills!")

      }
  }
}
