package models
import services.HotelTables
import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

import java.time.{LocalDate, LocalDateTime}
class ReservationTable(tag: Tag) extends Table[Reservation](tag, Some("hotel"),"reservation"){
  implicit val mappedColumnStatus = MappedColumnType.base[ReservationStatus.status, String](
    reservation => reservation.toString(),
    string => ReservationStatus.withName(string)
  )
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def startDate = column[LocalDate]("start_date")
  def endDate = column[Option[LocalDate]]("end_date")
  def status = column[ReservationStatus.status]("status")
  def customerId = column[Int]("customer_id")
  def roomId = column[Int]("room_id")
  foreignKey("customerId", customerId, HotelTables.CustomerTable)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
  foreignKey("roomId", roomId, HotelTables.RoomTable)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
  override def * : ProvenShape[Reservation] = (id, startDate, endDate, status, customerId, roomId) <> (Reservation.tupled, Reservation.unapply)
}
