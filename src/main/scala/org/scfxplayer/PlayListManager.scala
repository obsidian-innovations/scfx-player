package org.scfxplayer

import scala.util.Try
import play.api.libs.json._
import org.apache.commons.codec.binary.Base64


case class PlayList(files:List[String])
object PlayList {
  val jsFiles = "items"

  import Json._
  import Reads._
  import Writes._

  implicit val format = new Format[PlayList] {
    def reads(json: JsValue): JsResult[PlayList] = (json \ jsFiles).validate[List[String]].map(PlayList(_)).map { pl =>
      pl.copy(files = pl.files.map(s => new String(Base64.decodeBase64(s))))
    }

    def writes(o: PlayList): JsValue = obj(jsFiles -> o.files.map(s => new String(Base64.encodeBase64(s.getBytes))))
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

  private def readFile(file:String ):Try[String] = Try {
    val reader = new java.io.BufferedReader( new java.io.FileReader(file))
    var line:String = null;
    val stringBuilder = new StringBuilder();
    val ls = System.getProperty("line.separator");
    line = reader.readLine()
    while( line != null ) {
      stringBuilder.append( line );
      stringBuilder.append( ls );
      line = reader.readLine()
    }

    stringBuilder.toString();
  }

  def open(filename:String):Try[PlayList] =  {
    //val lines = scala.io.Source.fromFile(filename,"UTF-8").mkString
    readFile(filename).map{ lines =>
      Try(Json.parse(lines).as[PlayList])
    }.flatten
  }

  val playerHomeName = ".scfx-player"
  val defaultPlaylistName = "scfx-def-playlist.json"

  def defaultLocation(implicit fileHandling:FileHandling = JvmFileHandling):Try[String] = Try {
    val homeFolderPath = fileHandling.homeFolder
    val homeFolder = new java.io.File(homeFolderPath)
    val fileSep = fileHandling.fileSep
    val playerHomePath = homeFolder + fileSep + playerHomeName
    val playerHome = new java.io.File(playerHomePath)
    if(!playerHome.exists) playerHome.mkdir()
    playerHome.getAbsolutePath + fileSep + defaultPlaylistName
  }

  def saveToDefault(playlist:PlayList)(implicit fileHandling:FileHandling = JvmFileHandling):Try[Unit] =
    defaultLocation.flatMap(loc => save(loc,playlist))

  def openDefault(implicit fileHandling:FileHandling = JvmFileHandling):Try[PlayList] = defaultLocation.flatMap(loc => open(loc))


}
