import AssemblyKeys._

name := "scfx-player"

version := "1.1-SNAPSHOT"

scalaVersion := "2.11.6"

assemblySettings

resolvers ++= Seq(
    "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/",
    "Mandubian repository releases" at "https://github.com/mandubian/mandubian-mvn/raw/master/releases/",
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
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

//unmanagedJars in Compile += Attributed.blank(file(scala.util.Properties.javaHome) / "lib" / "jfxrt.jar")

javaOptions += "-Dfile.encoding=UTF-8"

mainClass in (Compile, run) := Some("org.scfxplayer.gui.Main")

mainClass in assembly := Some("org.scfxplayer.gui.Main")

fork in run := true

