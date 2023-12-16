package models

object ReservationStatus extends Enumeration {
  type status = Value
  val Active = Value("active")
  val Completed = Value("completed")
}
