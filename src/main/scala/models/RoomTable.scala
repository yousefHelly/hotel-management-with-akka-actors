package models
import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
class RoomTable(tag: Tag) extends Table[Room](tag, Some("hotel"), "room"){
  implicit val mappedTypesColumn = MappedColumnType.base[RoomTypes.room, String](
    room=>room.toString(),
    string => RoomTypes.withName(string)
  )
  implicit val mappedStatusColumn = MappedColumnType.base[RoomStatus.status,String](
    roomStatus=>roomStatus.toString(),
    string=>RoomStatus.withName(string)
  )
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def number = column[Int]("number")
  def roomType = column[RoomTypes.room]("type")
  def roomStatus = column[RoomStatus.status]("status")
  override def * : ProvenShape[Room] = (id, number, roomType, roomStatus)<>(Room.tupled, Room.unapply)
}
