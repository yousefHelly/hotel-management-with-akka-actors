package services
import scala.concurrent.Await
import slick.jdbc.MySQLProfile.api._
import Connection.db
import models.{Reservation, ReservationStatus, Room, RoomStatus}

import scala.concurrent.duration.DurationInt
object Room {
    implicit val mappedReservationStatusColumn = MappedColumnType.base[ReservationStatus.status, String](
      status => status.toString,
      string => ReservationStatus.withName(string)
    )
    implicit val mappedStatusColumn = MappedColumnType.base[RoomStatus.status, String](
      roomStatus => roomStatus.toString(),
      string => RoomStatus.withName(string)
    )
    def PostRoom = (room: Room) =>{
      val postQuery = (HotelTables.RoomTable returning HotelTables.RoomTable.map(_.id)) += (room)
      val result = try {
        Await.result(db.run(postQuery), 1.second)
      } catch {
        case _:Throwable => 0
      }
      result
    }

  def GetRoom= (roomId: Int) => {
    val GetQuery = HotelTables.RoomTable.filter(_.id===roomId).result
    val result = try {
      Await.result(db.run(GetQuery), 1.second)
    } catch {
      case _: Throwable => Seq.empty[Room]
    }
    result.head
  }

  def GetReservedRoom = (reservationId: Int, roomId: Int) => {
    val GetQuery = for {
      (res, room) <- HotelTables.ReservationTable.join(HotelTables.RoomTable).on(_.roomId===_.id)
      if(res.status === ReservationStatus.Active && res.roomId===roomId && res.id===reservationId)
    } yield (room.number)
    val result = try {
      Await.result(db.run(GetQuery.result), 1.second)
    } catch {
      case _: Throwable => Seq.empty[(Int)]
    }
    result.headOption
  }

  def GetRoomByNumber = (roomNumber: Int) => {
    val GetQuery = HotelTables.RoomTable.filter(_.number === roomNumber).result
    val result = try {
      Await.result(db.run(GetQuery), 1.second)
    } catch {
      case _: Throwable => Seq.empty[Room]
    }
    result.head
  }

  def getRoomWithReservationCount  = (roomId: Int) =>{
    val GetQuery = HotelTables.RoomTable.filter(_.id === roomId).result
    val GetReservations = HotelTables.RoomTable.join(HotelTables.ReservationTable).on(_.id===_.roomId).filter(res=>res._2.status==ReservationStatus.Active).result
    val resResult = try {
      Await.result(db.run(GetReservations), 1.second).length
    } catch {
      case _: Throwable => 0
    }

    val result = try {
      Await.result(db.run(GetQuery), 1.second)
    } catch {
      case _: Throwable => Seq.empty[Room]
    }
    (result.head, resResult)
  }

  def UpdateRoomStatus = (roomId: Int, status: RoomStatus.status) => {
    val CurrentRoomStatus = for { room <- HotelTables.RoomTable if(room.id===roomId) } yield room.roomStatus
    val GetQuery = CurrentRoomStatus.update(status)
    val result = try {
      Await.result(db.run(GetQuery), 1.second)
    } catch {
      case _: Throwable => 0
    }
    result
  }

  def GetAllRooms = () => {
    val GetQuery = HotelTables.RoomTable.map(_.id).result
    val result = try {
      Await.result(db.run(GetQuery), 1.second)
    } catch {
      case _: Throwable => Seq.empty[Int]
    }
    result
  }
}
