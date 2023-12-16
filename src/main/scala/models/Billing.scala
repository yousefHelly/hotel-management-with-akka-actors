package models

import java.time.LocalDate

case class Billing (id: Int = 1, amount: Double, date: LocalDate, reservationId: Int)
