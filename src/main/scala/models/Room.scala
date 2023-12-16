package models

case class Room(id: Int=1, number: Int, roomType: RoomTypes.room, status: RoomStatus.status)
