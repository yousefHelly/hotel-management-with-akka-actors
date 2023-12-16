package models

object RoomStatus extends Enumeration {
  type status = Value
  val Booked = Value("booked")
  val CheckedIn = Value("checked-in")
  val Available = Value("available")
}