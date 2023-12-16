package models

import java.time.LocalDate

case class Reservation (id: Int=1, startDate: LocalDate, endDate: Option[LocalDate] = None, status: ReservationStatus.status, customerId: Int, roomId: Int)
