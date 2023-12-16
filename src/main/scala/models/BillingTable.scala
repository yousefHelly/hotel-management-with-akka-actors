package models
import services.HotelTables
import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
import java.time.LocalDate

class BillingTable(tag: Tag) extends Table[Billing](tag, Some("hotel"), "billing"){
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def amount = column[Double]("amount")
  def date = column[LocalDate]("date")
  def reservationId = column[Int]("reservation_id")
  foreignKey("reservationId", reservationId, HotelTables.ReservationTable)(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
  override def * : ProvenShape[Billing] = (id, amount, date, reservationId) <> (Billing.tupled, Billing.unapply)
}
