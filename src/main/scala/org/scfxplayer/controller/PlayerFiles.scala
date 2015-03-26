package org.scfxplayer.controller

import scala.util.{Failure, Success, Try}
import play.api.libs.json.{Format, Json, Writes}
import org.scfxplayer.utils.{JvmFileHandling, FileHandling}

trait PlayerFiles {
  val playerHomeName = ".scfx-player"

  protected def readFile(file:String ):Try[String] = Try {
    val reader = new java.io.BufferedReader( new java.io.FileReader(file))
    var line:String = null
    val stringBuilder = new StringBuilder()
    val ls = System.getProperty("line.separator")
    line = reader.readLine()
    while( line != null ) {
      stringBuilder.append( line )
      stringBuilder.append( ls )
      line = reader.readLine()
    }

    stringBuilder.toString()
  }

  def save[T:Format](filename:String, items:T):Try[Unit] = Try {
    import Writes._
    val playlist = Json.stringify(Json.toJson(items))
    val output = new java.io.PrintWriter(new java.io.File(filename))
    try {
      output.write(playlist)
    } finally {
      output.close()
    }
  }

  def open[T:Format](filename:String):Try[T] =  {
    //val lines = scala.io.Source.fromFile(filename,"UTF-8").mkString
    readFile(filename).map{ lines =>
      Try(Json.parse(lines).as[T])
    }.flatten
  }

  def location(fileLocation:String):Try[String] = {
    val file = new java.io.File(fileLocation)
    if(file.canRead) Success(fileLocation)
    else Failure(new java.io.FileNotFoundException(s"$fileLocation cannot be read"))
  }

  def defaultLocation(filename:String)(implicit fileHandling:FileHandling = JvmFileHandling):Try[String] = Try {
    val homeFolderPath = fileHandling.homeFolder
    val homeFolder = new java.io.File(homeFolderPath)
    val fileSep = fileHandling.fileSep
    val playerHomePath = homeFolder + fileSep + playerHomeName
    val playerHome = new java.io.File(playerHomePath)
    if(!playerHome.exists) playerHome.mkdir()
    playerHome.getAbsolutePath + fileSep + filename
  }
}
