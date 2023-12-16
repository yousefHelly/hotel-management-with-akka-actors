package services
import scala.concurrent.Await
import slick.jdbc.MySQLProfile.api._
import Connection.db
import models.{Reservation, ReservationStatus}

import java.time.LocalDate
import scala.concurrent.duration.DurationInt
object Reservation {
  implicit val mappedStatusColumn = MappedColumnType.base[ReservationStatus.status, String](
    status => status.toString,
    string => ReservationStatus.withName(string)
  )
  def PostReservation= (reservation: Reservation) => {
    val CheckIfItsReserved = HotelTables.ReservationTable.filter(_.roomId===reservation.roomId).result
    val allRoomReservation = Await.result(db.run(CheckIfItsReserved), 1.second)
    if( allRoomReservation.nonEmpty ){
      // check if the date that customer sent is before today
      reservation.endDate match {
        case Some(date) =>
          if (LocalDate.now().isAfter(reservation.startDate) && date.isAfter(reservation.startDate))
            0
            // check if the room is reserved by another customer on this date
            val res2 = for {room <- allRoomReservation if {
              room.endDate match {
                case Some(date) =>
                  reservation.startDate.isBefore(date)
                case None =>  false
              }

            }} yield -1
            if (res2.contains(-1)) {
              -1
            } else {
              PostRes(reservation)
            }
        case None =>
          if (LocalDate.now().isAfter(reservation.startDate))
            0
          // check if the room is reserved by another customer on this date
          val res2 = for {room <- allRoomReservation if {
            room.endDate match {
              case Some(date) =>
                reservation.startDate.isBefore(date)
              case None => false
            }

          }} yield -1
          if (res2.contains(-1)) {
            -1
          } else {
            PostRes(reservation)
          }
      }
    } else{
      PostRes(reservation)
    }
  }
  def PostRes = (reservation: Reservation)=>{
    val postQuery = (HotelTables.ReservationTable returning HotelTables.ReservationTable.map(_.id)) += reservation
    val result = try {
      Await.result(db.run(postQuery), 1.second)
    } catch {
      case _: Throwable => 0
    }
    result
  }

  def GetReservation = (id: Int) => {
    val GetQuery = HotelTables.ReservationTable.filter(_.id===id).result
    val result = try {
      Await.result(db.run(GetQuery), 1.second)
    } catch {
      case _: Throwable => Seq.empty[Reservation]
    }
    result.head
  }

  def GetReservations = () => {
    val GetQuery = for {
     (reservation, room) <- HotelTables.ReservationTable.join(HotelTables.RoomTable).on(_.roomId===_.id)
     if(reservation.status===ReservationStatus.Active)
    } yield (reservation.id, reservation.customerId, room.number, reservation.startDate)
    val result = try {
      Await.result(db.run(GetQuery.result), 1.second)
    } catch {
      case _: Throwable => Seq.empty[(Int, Int, Int, LocalDate)]
    }
    result
  }

  def GetCustomerReservation = (customerId: Int, roomNumber: Int)=>{
    val GetQuery = for {
      (reservation, room) <- HotelTables.ReservationTable.join(HotelTables.RoomTable).on(_.roomId === _.id)
      if (reservation.status === ReservationStatus.Active && room.number===roomNumber && reservation.customerId===customerId )
    } yield (reservation.customerId, room.id, reservation.startDate)
    val result = try {
      Await.result(db.run(GetQuery.result), 1.second).filter( d => d._3.isBefore(LocalDate.now()) || d._3.equals(LocalDate.now())).headOption
    } catch {
      case err: Throwable => println(err)
        None
    }
    result
  }

  def SetReservationCompleted = (reservationId: Int, reservationStatus: ReservationStatus.status, endDate: Option[LocalDate])=> {
    val oldStatusQuery = for {
      reservation <- HotelTables.ReservationTable
      if (reservation.status === ReservationStatus.Active  && reservation.id === reservationId)
    } yield reservation.status
    if(endDate.nonEmpty){
      val EndDateQuery = for {
        reservation <- HotelTables.ReservationTable
        if (reservation.status === ReservationStatus.Active && reservation.id === reservationId)
      } yield reservation.endDate
      val newDate = EndDateQuery.update(endDate)
      val result = try {
        Await.result(db.run(newDate), 1.second)
      } catch {
        case _: Throwable => println("failed to update the endDate of the reservation")
      }
    }
    val newStatus = oldStatusQuery.update(reservationStatus)
    val result = try {
      Await.result(db.run(newStatus), 1.second)
    } catch {
      case _: Throwable => 0
    }
    result
  }
  def HasReservation = (customerId: Int, roomNumber: Int)=> {
    val GetQuery = for {
      (res, room) <- HotelTables.ReservationTable.join(HotelTables.RoomTable).on(_.roomId===_.id)
      if(res.status===ReservationStatus.Active && room.number===roomNumber && res.customerId===customerId)
    } yield res.id
    val result = try {
      Await.result(db.run(GetQuery.result), 1.second).length
    } catch {
      case _: Throwable => 0
    }
    result
  }

}
