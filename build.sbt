import AssemblyKeys._

name := "scfx-player"

version := "1.0"

scalaVersion := "2.10.2"

assemblySettings

resolvers ++= Seq(
    "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/",
    "Mandubian repository releases" at "https://github.com/mandubian/mandubian-mvn/raw/master/releases/"
    )



libraryDependencies ++= Seq(
                           "org.specs2" %% "specs2" % "2.2.2" % "test",
                            "joda-time" % "joda-time" % "2.3",
                            "org.joda" % "joda-convert" % "1.4",
                            "play" %% "play-json" % "2.2-SNAPSHOT",
                            "commons-codec" % "commons-codec" % "1.8",
//to be removed
//                            "org" % "jaudiotagger" % "2.0.3",
// current stuff
                             "org.scalafx" % "scalafx_2.10" % "1.0.0-M5"
// new stuff
//                            "org.scalafx" % "scalafx_2.10" % "8.0.0-M1"
                            )

//to be removed
//unmanagedJars in Compile += Attributed.blank(file(System.getenv("JAVA_HOME") + "/jre/lib/jfxrt.jar"))
unmanagedJars in Compile += Attributed.blank(file(scala.util.Properties.javaHome) / "lib" / "jfxrt.jar")

javaOptions += "-Dfile.encoding=UTF-8"

mainClass in (Compile, run) := Some("org.scfxplayer.Main")

mainClass in assembly := Some("org.scfxplayer.Main")

fork in run := true

