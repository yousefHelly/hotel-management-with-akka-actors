# Hotel Management System

A Scala-based Hotel Management System designed to digitalize hotel operations. This system includes features such as room booking, maintenance, customer management, billing, and report generation. It provides a secure and efficient way to manage hotel operations, ensuring customer satisfaction.

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)

## Features

- Room booking
- Room maintenance
- Customer management
- Billing
- Report generation

## Getting Started

### Prerequisites

- Scala
- Akka
- Slick
- [Hotel DB SQL File](https://drive.google.com/file/d/1-WIGz_YdnLld7kU67IO6em9gGG8CbBWb)

### Installation

1. Clone the repository to your local machine:

   ```bash
   git clone https://github.com/yousefHelly/hotel-management-with-akka-actors.git

2. **Navigate to the project directory:**

   ```bash
   cd hotel-management-with-akka-actors

3. **Configure MySQL Database Connection**:    

   - Open the `application.conf` file in your project directory.
     
   - Update the configuration with your MySQL database connection details:


2. Setting Up MySQL Database with XAMPP:
  - Start XAMPP and ensure that Apache and MySQL server are running.
  
  - Open the phpMyAdmin interface by visiting http://localhost/phpmyadmin/ in your web browser.
  
  - Create a new database for the project with the name ``hotel`` and Import the database file that contains all the necessary tables for the application to run.

## Usage

   - In the project directory, execute the following command to run the application:

     ```bash
     sbt run
     ```  
     
  - Initialize Hotel and each Customer to fetch it's last state 
    ```bash
    // initialize hotel in top of the code with charges per night for single and double room types
    hotel ! InitializeHotel(50, 75)
    
    // initialize each customer with his name and contact number
    ahmed ! initializeCustomer("ahmed", "01020273407")
    ```
  - Customer Actions


    ```bash
      // Check-in to a room
      ahmed ! CheckIn(hotel, 4)
      
      // Check-out from a room
      ahmed ! CheckOut(hotel, 4)
      
      // Make a reservation for a room
      ahmed ! MakeReservation(hotel, 5, LocalDate.now(), LocalDate.of(2023, 12, 17))
      
      // Request maintenance for a room
      ahmed ! Maintenance(hotel, 5, "the TV is not working")
      
      // Check-out from a reserved room
      ahmed ! CheckOut(hotel, 5)
    ```

- Hotel Actions


    ```bash
    // Uncomment and customize as needed
    // hotel ! AddRoom(RoomTypes.Double)
    // hotel ! GetRoomStatus(5)
    // hotel ! GenerateReport

    ```
