package services
import scala.concurrent.Await
import slick.jdbc.MySQLProfile.api._
import Connection.db
import models.Customer
import scala.concurrent.duration.DurationInt
object Customer {
  def PostCustomer: Customer => Int = (customer: Customer) => {
    val postQuery = (HotelTables.CustomerTable returning HotelTables.CustomerTable.map(_.id)) += customer
    val result = try {
      Await.result(db.run(postQuery), 1.second)
    } catch {
      case _: Throwable => 0
    }
    result
  }

  def GetCustomer = (customer: Customer) => {
    val postQuery = HotelTables.CustomerTable.filter(_.name===customer.name).filter(_.contactNumber===customer.contactNumber).result
    val result = try {
      Await.result(db.run(postQuery), 1.second).headOption
    } catch {
      case _: Throwable => None
    }
    result
  }

}
