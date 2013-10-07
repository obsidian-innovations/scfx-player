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

class PlayListManagerMenu(val musicRecItems: ObservableBuffer[MusicRecordItem]) {

  import scalafx.Includes._

  import FileChoosers._

  def loadFiles(fs:Seq[java.io.File]) = {
    fs.foreach { f => TrackMetaData(f) { mdTry => mdTry.foreach{ md =>
      val item = new MusicRecordItem(md.artist, md.album, md.track, md.duration, f.getName, f.getAbsolutePath)
      if(!musicRecItems.map(_.fullPath).contains(item.fullPath)) { musicRecItems += item }
    }}}
  }

  def loadPlayList(pl:PlayList):Unit = {
    if(!pl.files.isEmpty) musicRecItems.clear()
    loadFiles(pl.files.map(new java.io.File(_)))
  }

  def loadDefaultPlaylist():Unit = {
    PlayListManager.openFromSettings().map{ playlist =>
      loadFiles(playlist.files.map(new java.io.File(_)))
    }
  }

  def menu(parent:Parent) = {

//    val openFilesBtn = new Button {
//      styleClass ++= List("player-button", "button-eject")
//      text = "Add Files"
//      prefWidth = 150
//      prefHeight = 24
//      onMouseClicked = new EventHandler[MouseEvent] {
//        override def handle(event:MouseEvent) {
//          event.consume()
//          Try(fchooser.showOpenMultipleDialog(parent.value.getScene.getWindow)).map { fs =>
//            loadFiles(fs)
//          }
//        }
//      }
//    }

    val addFileMenuItem = new MenuItem("Add Files") {
      onAction = (event: ActionEvent) => {
        event.consume()
        Try(fchooser.showOpenMultipleDialog(parent.getScene.getWindow)).map { fs =>
          loadFiles(fs)
        }
      }
    }

    val openPlaylistMenuItem = new MenuItem("Open playlist") {
      onAction = (event: ActionEvent) => {
        event.consume()
        Try(plchooser.showOpenDialog(parent.getScene.getWindow)).map { file =>
          PlayListManager.open[PlayList](file.getAbsolutePath).map { pl =>
            loadPlayList(pl)
            PlayListManager.saveInSettings(PlayListFile(file.getAbsolutePath,pl))
          }
        }
      }
    }

    val savePlaylistMenuItem = new MenuItem("Save playlist") {
      onAction = (event: ActionEvent) => {
        event.consume()
        Try(plsaver.showSaveDialog(parent.getScene.getWindow)).map { file =>
          val pl = PlayList(musicRecItems.map(_.fullPath).toList)
          val selectedFilename = file.getAbsolutePath
          val filename = if(selectedFilename.endsWith(playlistExt)) selectedFilename else selectedFilename + "." + playlistExt
          val plFile = PlayListFile(filename, pl)
          PlayListManager.saveInSettings(plFile)
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
}
