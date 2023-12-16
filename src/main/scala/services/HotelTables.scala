package services

import models.{BillingTable, CustomerTable, ReservationTable, RoomTable}
import slick.lifted.TableQuery
object HotelTables {
  // API start point
  lazy val CustomerTable = TableQuery[CustomerTable]
  lazy val RoomTable = TableQuery[RoomTable]
  lazy val ReservationTable = TableQuery[ReservationTable]
  lazy val BillingTable = TableQuery[BillingTable]
}
