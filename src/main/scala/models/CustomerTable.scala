package models
import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
class CustomerTable(tag: Tag) extends Table[Customer](tag, Some("hotel"), "customer"){
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def contactNumber = column[String]("contact_number")
  override def * : ProvenShape[Customer] = (id, name, contactNumber) <> (Customer.tupled, Customer.unapply)
}
