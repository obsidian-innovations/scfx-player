package org.scfxplayer

import java.io.File
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.stage.{FileChooser, WindowEvent}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableColumn._
import scalafx.scene.control._
import scalafx.scene.layout.{Region, Priority, HBox, VBox}
import scalafx.geometry.{Side, Pos}
import javafx.scene.control.CheckMenuItem
import scalafx.event.{Event, ActionEvent}
import scala.util.Try
import scalafx.collections.ObservableBuffer.Remove

object Main extends JFXApp {
  import scalafx.Includes._

  val fchooser:FileChooser = new FileChooser()
  val f = new FileChooser.ExtensionFilter("MP3 (MPEG-1 or MPEG-2 Audio Layer III)", Seq("*.mp3", "*.MP3"))
  fchooser.getExtensionFilters.addAll(f)
  fchooser.setTitle("Demo ScalaFX Player")
  fchooser.setInitialDirectory(new File(System.getProperty("user.home")))

  def onRemovingItems(idx:Int, els:Traversable[MusicRecordItem]) = {
    els.filter(x => playerControls.isPlaying(x) && x.isMarkedDeleted).foreach(_ => playerControls.stop())
    if(musicRecItems.size == 0) {
      playerControls.disable = true
      deleteFilesBtn.disable = true
    }
  }

  def onAddingItems(idx:Int, els:Traversable[MusicRecordItem]) = {
    if(musicRecItems.size > 0) {
      playerControls.disable = false
      deleteFilesBtn.disable = false
    }
  }

  val musicRecItems = ObservableBuffer[MusicRecordItem]()
  musicRecItems.onChange {
    (_, changes) => {
      for (change <- changes) change match {
        case ObservableBuffer.Remove(position, els) =>
          onRemovingItems(position, els.asInstanceOf[Traversable[MusicRecordItem]])
        case ObservableBuffer.Add(position, els) =>
          onAddingItems(position, els.asInstanceOf[Traversable[MusicRecordItem]])
        case _ => {}
      }
    }
  }
  val playList = new PlayListWidget(musicRecItems)
  val musicRecTable = playList.tableView((i:MusicRecordItem) => playerControls.play(i))

  val playerControls = new PlayerControls(musicRecItems) {
    alignment = Pos.CENTER
    minWidth = 200
    maxWidth = 500
    hgrow = Priority.ALWAYS
    vgrow = Priority.ALWAYS
  }

  val playlistSettingsBtn:Button = new Button {
    styleClass ++= List("player-button", "button-settings")
    maxWidth = 24
    minWidth = 24
    maxHeight = 24
    minHeight = 24
    onMouseClicked = new EventHandler[MouseEvent] {
      override def handle(event:MouseEvent) {
        event.consume()
        val topNode = parent.value.getScene.content.head
        if(!playList.playListSettingsMnu.showing.value)
          playList.playListSettingsMnu.show(topNode, Side.LEFT, event.getSceneX, event.getSceneY)
        else
          playList.playListSettingsMnu.hide()
      }
    }
  }

  val deleteFilesBtn = new Button {
    styleClass ++= List("player-button", "button-delete")
    maxWidth = 24
    minWidth = 24
    maxHeight = 24
    minHeight = 24
    onMouseClicked = new EventHandler[MouseEvent] {
      override def handle(event:MouseEvent) {
        event.consume()
        val smodel = musicRecTable.selectionModel.value
        smodel.getSelectedIndices.toList.sortWith((x, y) => x > y).foreach { x =>
          musicRecItems.lift(x).foreach(_.markDeleted)
          musicRecItems.remove(x)
        }
        smodel.clearSelection()
      }
    }
  }

  val openFilesBtn = new Button {
    styleClass ++= List("player-button", "button-eject")
    maxWidth = 24
    minWidth = 24
    maxHeight = 24
    minHeight = 24
    onMouseClicked = new EventHandler[MouseEvent] {
      override def handle(event:MouseEvent) {
        event.consume()
        Try(fchooser.showOpenMultipleDialog(parent.value.getScene.getWindow)).map { fs =>
          loadFiles(fs)
        }
      }
    }
  }

  val playerControlsLayout = new HBox {
    val lspacer = new Region {hgrow = Priority.ALWAYS}
    val rspacer = new Region {hgrow = Priority.ALWAYS}
    hgrow = Priority.ALWAYS
    alignment = Pos.BOTTOM_CENTER
    minHeight = 70
    maxHeight = 70
//    fillHeight = true // Doesn't work?!?
    content ++= List(openFilesBtn, lspacer, playerControls, rspacer, playlistSettingsBtn, deleteFilesBtn)
  }

  val mainLayout = new VBox {
    vgrow = Priority.ALWAYS
    hgrow = Priority.ALWAYS
    content = Seq(playerControlsLayout, musicRecTable)
  }

  stage = new PrimaryStage {
    title = "Demo ScalaFX Player"
    minHeight = 300
    minWidth = 400
    width = 640
    height = 480
    scene = new Scene {
      stylesheets ++= List("default-skin.css")
      width onChange {mainLayout.setPrefWidth(scene.value.getWidth);}
      height onChange {mainLayout.setPrefHeight(scene.value.getHeight);}
      content = mainLayout
      onCloseRequest = (event:WindowEvent) => {
        playerControls.stop()
        val pl = PlayList(musicRecItems.map(_.fullPath).toList)
        PlayListManager.saveToDefault(pl)
      }
      onShowing = (event:WindowEvent) => { loadDefaultPlaylist() }
    }
  }

  def loadFiles(fs:Seq[java.io.File]) = {
    fs.foreach { f => TrackMetaData(f) { mdTry => mdTry.foreach{ md =>
      val item = new MusicRecordItem(md.artist, md.album, md.track, md.duration, f.getName, f.getAbsolutePath)
      if(!musicRecItems.map(_.fullPath).contains(item.fullPath)) { musicRecItems += item }
    }}}
  }

  def loadDefaultPlaylist():Unit = {
    PlayListManager.openDefault().map{ playlist =>
      loadFiles(playlist.files.map(new java.io.File(_)))
    }
  }

}
