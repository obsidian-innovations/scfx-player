package org.scfxplayer

import javafx.scene.control.{CustomMenuItem, CheckMenuItem}
import scalafx.scene.control.{MenuItem, Button, ContextMenu}
import scalafx.collections.ObservableBuffer
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import scala.util.Try
import scalafx.stage.{Window, FileChooser}
import java.io.File
import scalafx.scene.Parent
import scalafx.event.ActionEvent
import org.slf4j.LoggerFactory
import scalafx.beans.property.StringProperty
import org.scfxplayer.async.{Processed, AddFilesActor}
import akka.actor.Props

object FileChoosers {
  val fchooser:FileChooser = new FileChooser()
  val f = new FileChooser.ExtensionFilter("MP3 (MPEG-1 or MPEG-2 Audio Layer III)", Seq("*.mp3", "*.MP3"))
  fchooser.getExtensionFilters.addAll(f)
  fchooser.setTitle("Demo ScalaFX Player")
  fchooser.setInitialDirectory(new File(System.getProperty("user.home")))

  val playlistExt = "playlist"

  val plchooser:FileChooser = new FileChooser()
  val pl = new FileChooser.ExtensionFilter("playlists", Seq(s"*.${playlistExt}"))
  plchooser.getExtensionFilters.addAll(pl)
  plchooser.setTitle("Open playlist")
  plchooser.setInitialDirectory(new File(System.getProperty("user.home")))

  val plsaver:FileChooser = new FileChooser()
  plsaver.getExtensionFilters.addAll(pl)
  plsaver.setTitle("Save playlist")
  plsaver.setInitialDirectory(new File(System.getProperty("user.home")))
}

class PlayListManagerMenu(val musicRecItems: ObservableBuffer[MusicRecordItem]) { mgr =>

  private val logger = LoggerFactory.getLogger(this.getClass);

  val currentPlayListName = ObservableBuffer[String]()

  import scalafx.Includes._

  import FileChoosers._

  private val asystem = akka.actor.ActorSystem("scfx")

  def loadFiles(fs:Seq[java.io.File]) = {
    val actor = asystem.actorOf(Props(new AddFilesActor(fs,this)))

    logger.info(s"loading the following files \n${fs.map(_.getAbsolutePath).mkString("\n")}")
    fs.foreach { f => TrackMetaData(f) { mdTry => mdTry.foreach{ md =>
      val item = new MusicRecordItem(md.artist, md.album, md.track, md.duration, f.getName, f.getAbsolutePath)
      if(!musicRecItems.map(_.fullPath).contains(item.fullPath)) {
        musicRecItems += item
        actor ! Processed
      }
    }}}
  }

  def loadPlayList(plf:PlayListFile):Unit = {
    val PlayListFile(loc,pl) = plf
    logger.info(s"loading playlist: ${pl.files}")
    if(!pl.files.isEmpty) {
      logger.info("clearing the items in the player")
      musicRecItems.clear()
      this.currentPlayListName += loc
    }
    loadFiles(pl.files.map(new java.io.File(_)))
  }

  def loadDefaultPlaylist():Unit = {
    logger.info("loading the currently selected playlist - in settings")
    PlayListManager.openFromSettings().map{ case PlayListFile(loc,playlist) =>
      this.currentPlayListName  += loc
      loadFiles(playlist.files.map(new java.io.File(_)))
    }
  }

  def saveCurrentPlaylist() = {
    val pl = PlayList(musicRecItems.map(_.fullPath).toList)
    PlayListManager.saveCurrent(pl)
  }

  def menu(parent:Parent) = {

    val addFileMenuItem = new MenuItem("Add Files") {
      onAction = (event: ActionEvent) => {
        event.consume()
        logger.info("adding files menu item")
        Try(fchooser.showOpenMultipleDialog(parent.getScene.getWindow)).map { fs =>
          loadFiles(fs)
        }
      }
    }

    val openPlaylistMenuItem = new MenuItem("Open playlist") {
      onAction = (event: ActionEvent) => {
        event.consume()
        logger.info("open playlist menu item")
        Try(plchooser.showOpenDialog(parent.getScene.getWindow)).map { file =>
          PlayListManager.open[PlayList](file.getAbsolutePath).map { pl =>
            loadPlayList(PlayListFile(file.getAbsolutePath,pl))
            PlayListManager.saveInSettings(PlayListFile(file.getAbsolutePath,pl))
          }
        }
      }
    }

    val savePlaylistMenuItem = new MenuItem("Save playlist") {
      onAction = (event: ActionEvent) => {
        logger.info("save playlist menu item")
        event.consume()
        Try(plsaver.showSaveDialog(parent.getScene.getWindow)).map { file =>
          val pl = PlayList(musicRecItems.map(_.fullPath).toList)
          val selectedFilename = file.getAbsolutePath
          val filename = if(selectedFilename.endsWith(playlistExt)) selectedFilename else selectedFilename + "." + playlistExt
          val plFile = PlayListFile(filename, pl)
          logger.info(s"saving playlist $plFile")
          PlayListManager.saveInSettings(plFile)
          mgr.currentPlayListName += filename
        }
      }
    }

    val menu:ContextMenu = new ContextMenu {
      styleClass ++= Seq("file-menu")
      autoHide = true
      items ++= Seq(addFileMenuItem, openPlaylistMenuItem, savePlaylistMenuItem)
    }

    menu
  }

  def shutdown() = {
    asystem.shutdown()
  }
}
