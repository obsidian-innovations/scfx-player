import AssemblyKeys._

name := "scfx-player"

version := "1.0"

scalaVersion := "2.10.2"

assemblySettings

libraryDependencies ++= Seq(
                           "org.specs2" %% "specs2" % "2.2.2" % "test",
                            "org" % "jaudiotagger" % "2.0.3",
// current stuff
                             "org.scalafx" % "scalafx_2.10" % "1.0.0-M5"
// new stuff
//                            "org.scalafx" % "scalafx_2.10" % "8.0.0-M1"
                            )

//unmanagedJars in Compile += Attributed.blank(file(System.getenv("JAVA_HOME") + "/jre/lib/jfxrt.jar"))
unmanagedJars in Compile += Attributed.blank(file(scala.util.Properties.javaHome) / "lib" / "jfxrt.jar")


mainClass in (Compile, run) := Some("org.scfxplayer.ScFxMediaTagTest")

mainClass in assembly := Some("org.scfxplayer.Main")

fork in run := true

