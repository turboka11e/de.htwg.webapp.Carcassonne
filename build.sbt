name := """play-carcassonne"""
organization := "de.htwg.webapp"

version := "1.0-SNAPSHOT"
scalaVersion := "2.12.2"

lazy val branch = "SA-02-Monads"
lazy val carcassonne = RootProject(uri("git://github.com/turboka11e/de.htwg.se.Carcassonne.git#%s".format(branch)))

lazy val root = Project("play-carcassonne", file("."))
  .enablePlugins(PlayScala)
  .aggregate(carcassonne)
  .dependsOn(carcassonne)

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "de.htwg.webapp.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "de.htwg.webapp.binders._"
