import actors.{Customer, Hotel}
import actors.Hotel._
import akka.actor.{ActorSystem, Props}
import models.RoomTypes
import actors.Customer.{BookRoom, CheckIn, CheckOut, Maintenance, MakeReservation, initializeCustomer}

import java.time.{LocalDate, LocalDateTime}
object Main extends App{
    val system = ActorSystem("mainSystem")
    val hotel = system.actorOf(Props[Hotel], "Hotel")
    val ahmed = system.actorOf(Props[Customer], "ahmed")
    /**
     * hotel messages
     * */
    hotel ! InitializeHotel(50, 75)
    //hotel ! AddRoom(RoomTypes.Double)
    //hotel ! GetRoomStatus(5)
    //hotel ! GenerateReport
    /**
     * customer messages
     * */
    ahmed ! initializeCustomer("ahmed", "01020273407")
    //ahmed ! CheckIn(hotel, 4)
     //ahmed ! CheckOut(hotel, 4)
    //ahmed ! MakeReservation(hotel, 5, LocalDate.now(), LocalDate.of(2023, 12, 17))
    //ahmed ! Maintenance(hotel, 5, "the tv is not working")
    //ahmed ! CheckOut(hotel, 5)
//    system.terminate()
}