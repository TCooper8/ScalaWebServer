organization := "tcooper8"

name := "Scala Web Server"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.2"

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-netty-server" % "0.7.1",
  "net.databinder.dispatch" %% "dispatch-core" % "0.10.0",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "org.scala-lang.modules" %% "scala-async" % "0.9.0"
)