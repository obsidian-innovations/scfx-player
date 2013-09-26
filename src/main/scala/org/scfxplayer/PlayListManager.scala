package org.scfxplayer

import scala.util.Try
import play.api.libs.json._


object PlayListManager {
  def save(filename:String, items:List[MusicRecordItem]):Try[Unit] = Try {
    import Writes._
    val playlist = Json.stringify(Json.toJson(items))
    val output = new java.io.PrintWriter(new java.io.File(filename))
    try {
      output.write(playlist)
    } finally {
      output.close()
    }
  }

  def open(filename:String):Try[List[MusicRecordItem]] = Try {
    val lines = scala.io.Source.fromFile(filename).mkString
    Json.parse(lines).as[List[MusicRecordItem]]
  }

  val playerHomeName = ".scfx-player"
  val defaultPlaylistName = "scfx-def-playlist.json"


  def defaultLocation(fileHandling:FileHandling = JvmFileHandling):Try[String] = Try {
    val homeFolderPath = fileHandling.homeFolder
    val homeFolder = new java.io.File(homeFolderPath)
    val fileSep = fileHandling.fileSep


    val playerHomePath = homeFolder + fileSep + playerHomeName
    val playerHome = new java.io.File(playerHomePath)

    if(!playerHome.exists) playerHome.mkdir()

    playerHome.getAbsolutePath + fileSep + defaultPlaylistName
  }

  def saveToDefault(playlist:List[MusicRecordItem]):Try[Unit] = defaultLocation().flatMap(loc => save(loc,playlist))

  def openDefault():Try[List[MusicRecordItem]] = defaultLocation().flatMap(loc => open(loc))

}
