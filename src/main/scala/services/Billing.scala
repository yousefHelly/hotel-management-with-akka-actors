package services
import scala.concurrent.Await
import slick.jdbc.MySQLProfile.api._
import Connection.db
import models.Billing
import models.RoomTypes
import models.RoomTypes.room

import java.time.LocalDate
import scala.concurrent.duration.DurationInt

object Billing {
  implicit val mappedStatusColumn = MappedColumnType.base[RoomTypes.room, String](
    roomType => roomType.toString(),
    string => RoomTypes.withName(string)
  )
  def PostBill = (bill: Billing) => {
    val postQuery = HotelTables.BillingTable  += bill
    val result = try {
      Await.result(db.run(postQuery), 1.second)
    } catch {
      case _: Throwable => 0
    }
    result
  }
  def GetAllBills: () => Seq[(Int, Double, LocalDate, LocalDate, Int, room, String)] = ()=>{
    val GetQuery = for {
      (((bill, reservation), room), customer) <- HotelTables.BillingTable
                                                  .join(HotelTables.ReservationTable)
                                                  .on(_.reservationId===_.id)
                                                  .join(HotelTables.RoomTable)
                                                  .on(_._2.roomId===_.id)
                                                  .join(HotelTables.CustomerTable)
                                                  .on(_._1._2.customerId===_.id)
    } yield {
      (bill.id, bill.amount, reservation.startDate,bill.date, room.number, room.roomType, customer.name)
    }
    val result = try {
      Await.result(db.run(GetQuery.result), 1.second)
    } catch {
      case _: Throwable => Seq.empty[(Int, Double, LocalDate, LocalDate, Int, RoomTypes.room, String)]
    }
    result
  }
}
