ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "hotel-management-system-with-akka"
  )
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.8.0",
  "com.typesafe.slick" %% "slick" % "3.4.1",
  "org.slf4j" % "slf4j-nop" % "2.0.5",
  "com.mysql" % "mysql-connector-j" % "8.0.33",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1",
)