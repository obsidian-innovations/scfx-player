package org.scfxplayer

import scala.util.Try
import play.api.libs.json._


case class PlayList(files:List[String])
object PlayList {
  val jsFiles = "items"

  import Json._
  import Reads._
  import Writes._

  implicit val format = new Format[PlayList] {
    def reads(json: JsValue): JsResult[PlayList] = (json \ jsFiles).validate[List[String]].map(PlayList(_))

    def writes(o: PlayList): JsValue = obj(jsFiles -> o.files)
  }
}

object PlayListManager {
  def save(filename:String, items:PlayList):Try[Unit] = Try {
    import Writes._
    val playlist = Json.stringify(Json.toJson(items))
    val output = new java.io.PrintWriter(new java.io.File(filename))
    try {
      output.write(playlist)
    } finally {
      output.close()
    }
  }

  def open(filename:String):Try[PlayList] = Try {
    val lines = scala.io.Source.fromFile(filename,"UTF-8").mkString
    Json.parse(lines).as[PlayList]
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

  def saveToDefault(playlist:PlayList):Try[Unit] = defaultLocation().flatMap(loc => save(loc,playlist))

  def openDefault():Try[PlayList] = defaultLocation().flatMap(loc => open(loc))

}
