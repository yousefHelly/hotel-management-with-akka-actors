package actors

import actors.Hotel.InitializeRoom
import models.{RoomTypes, Room => RoomType, RoomStatus => Status}
import akka.actor.Actor
import akka.io.UdpConnected.Received
import services.Room._
import Hotel._
import Room._
import actors.Reservation.SetRoomStatusTo
class Room extends Actor{
  override def receive: Receive = {
    case IsInitialized(id: Int) =>
      GetRoom(id) match {
        case room =>
          context.become(InitializedRoom(room))
      }
    case InitializeRoom(number, roomType, roomStatus) =>
      PostRoom(RoomType(number = number, roomType = roomType, status = roomStatus)) match {
        case roomId if roomId>0 =>
          GetRoom(roomId) match {
            case room =>
              println(s"[ ${self.path.parent.name} -> ${self.path.name} ] i'v been initialized as a/an ${roomStatus}-${roomType}-room")
              context.become(InitializedRoom(room))
          }
        case 0 =>
          println(s"[ ${self.path.parent.name} -> ${self.path.name} ] Failed to add a new room!")
      }
    case _ => println(s"[ ${self.path.parent.name} -> ${self.path.name} ] Room hasn't been initialized yet!")
  }
  private def InitializedRoom(room: RoomType) : Receive = {
    case WhatIsYourStatus => sender() ! RoomStatus(room.number, room.status)
    case CheckInRoom(customerId) =>
      if( room.status == Status.Available ){
        UpdateRoomStatus(room.id, Status.CheckedIn) match {
          case i if i>0 =>
            context.become(InitializedRoom(room.copy(status = Status.CheckedIn)))
            println(s"[ ${self.path.parent.name} -> ${self.path.name} ] Room status changed to checked-in successfully!")
          case 0 => println(s"[ ${self.path.parent.name} -> ${self.path.name} ] Failed to change the room status!")
        }
      } else sender() ! RoomStatus(room.number, room.status)
    case SetRoomStatusTo(status, isCheckedOut) =>
      if((!room.status.equals(status) && room.status != Status.CheckedIn) || isCheckedOut)
        UpdateRoomStatus(room.id, status) match {
        case i if i > 0 =>
          context.become(InitializedRoom(room.copy(status = status)))
          println(s"[ ${self.path.parent.name} -> ${self.path.name} ] Room status changed to ${status} successfully!")
        case 0 => println(s"[ ${self.path.parent.name} -> ${self.path.name} ] Failed to change the room status!")
      }
  }
}
object Room {
  case class RoomStatus(number: Int, status: Status.status)
}
