package org.scfxplayer.controller

import org.slf4j.LoggerFactory

import scala.util.{Try, Success}
import play.api.libs.json._
import org.apache.commons.codec.binary.Base64
import org.scfxplayer.settings.Settings
import org.scfxplayer.utils.{JvmFileHandling, FileHandling}

case class PlayList(files:List[String])

object PlayList {
  val jsFiles = "items"

  import Json._
  import Writes._

  implicit val format = new Format[PlayList] {
    def reads(json: JsValue): JsResult[PlayList] = (json \ jsFiles).validate[List[String]].map(PlayList(_)).map { pl =>
      pl.copy(files = pl.files.map(s => new String(Base64.decodeBase64(s))))
    }

    def writes(o: PlayList): JsValue = obj(jsFiles -> o.files.map(s => new String(Base64.encodeBase64(s.getBytes))))
  }
}

case class PlayListFile(location:String, playlist:PlayList)

object PlayListFile extends PlayerFiles {
  val defaultLoc = defaultLocation(PlayListManager.defaultPlaylistName)
}

object PlayListManager extends PlayerFiles {
  private val logger = LoggerFactory.getLogger(this.getClass)
  val defaultPlaylistName = "scfx-def-playlist.playlist"

  def saveToDefault(playlist:PlayList)(implicit fileHandling:FileHandling = JvmFileHandling):Try[Unit] =
    defaultLocation(defaultPlaylistName).flatMap(loc => save(loc,playlist))

  def openDefault(implicit fileHandling:FileHandling = JvmFileHandling):Try[PlayListFile] = for {
    playlistFilePath <- defaultLocation(defaultPlaylistName)
    playlist <- open[PlayList](playlistFilePath)
  } yield {
      logger.debug(s"playlist location from default: $playlistFilePath")
      PlayListFile(playlistFilePath, playlist)
    }

  def openFromSettings(implicit fileHandling:FileHandling = JvmFileHandling):Try[PlayListFile] = for {
    playlistFilePath <- Settings.playlistLocation
    playlist <- open[PlayList](playlistFilePath)
  } yield {
      logger.debug(s"playlist location from settings: $playlistFilePath")
      PlayListFile(playlistFilePath, playlist)
    }

  def saveInSettings(plFile:PlayListFile)(implicit fileHandling:FileHandling = JvmFileHandling):Try[Unit] = for {
    playlistFilePath <- Settings.playlistLocation
    _ <- save(plFile.location, plFile.playlist)
    _ <- Settings.savePlaylistLocation(plFile.location)
  } yield ()


  def saveCurrent(playList:PlayList)(implicit fileHandling:FileHandling = JvmFileHandling):Try[Unit] = for {
    playlistFilePath <- Settings.playlistLocation
    _ <- save(playlistFilePath, playList)
  } yield ()
}
