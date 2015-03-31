
lazy val commonSettings: Seq[Setting[_]] = Seq(
  name := "scfx-player",
  version := "1.2-SNAPSHOT",
  organization := "com.oi",
  scalaVersion := "2.11.6"
)

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.4" % "test",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.4",
  "com.typesafe.play" %% "play-json" % "2.3.0",
  "commons-codec" % "commons-codec" % "1.8",
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "ch.qos.logback" % "logback-core" % "1.0.13",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.scalafx" %% "scalafx" % "8.0.40-R8"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    javaOptions += "-Dfile.encoding=UTF-8",
    mainClass in (Compile, run) := Some("org.scfxplayer.gui.Main"),
    mainClass := Some("org.scfxplayer.gui.Main"),
    fork in run := true
  )
