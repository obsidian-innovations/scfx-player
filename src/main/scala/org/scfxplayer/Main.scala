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
import scalafx.Includes._
import javafx.scene.control.CheckMenuItem
import scalafx.event.{Event, ActionEvent}
import scala.util.Try

//import scalafx.scene.media.{Media, MediaPlayer, MediaView, MediaErrorEvent}

object Main extends JFXApp {

//  val testMp3 = new File("test-data/test.mp3")
//  val media = new Media(testMp3.toURI.toURL.toExternalForm)
//  val mplayer = new MediaPlayer(media)
//  val playerView = new MediaView {
//    mediaPlayer = mplayer
//    fitHeight = 40
//    fitWidth = 300
//    hgrow = Priority.ALWAYS
//    vgrow = Priority.ALWAYS
//    smooth = true
//    onError = (event: MediaErrorEvent) => println("Media view error: " + event)
//  }

  val fchooser:FileChooser = new FileChooser()
  val f = new FileChooser.ExtensionFilter("MP3 (MPEG-1 or MPEG-2 Audio Layer III)", Seq("*.mp3", "*.MP3"))
  fchooser.getExtensionFilters.addAll(f)
  fchooser.setTitle("Demo ScalaFX Player")
  fchooser.setInitialDirectory(new File(System.getProperty("user.home")))

  val musicRecItems = ObservableBuffer[MusicRecordItem]()
  val durationColumn = new TableColumn[MusicRecordItem, String]() {
    text = "Duration"
    prefWidth = 80
    minWidth = 50
    cellValueFactory = {_.value.duration}
  }
  val trackColumn = new TableColumn[MusicRecordItem, String] {
    text = "Track"
    prefWidth = 300
    minWidth = 50
    cellValueFactory = {_.value.trackNameMade}
  }
  val albumColumn = new TableColumn[MusicRecordItem, String]() {
    text = "Album"
    prefWidth = 120
    minWidth = 50
    cellValueFactory = {_.value.album}
  }
  val artistColumn = new TableColumn[MusicRecordItem, String]() {
    text = "Artist"
    prefWidth = 120
    minWidth = 50
    cellValueFactory = {_.value.artist}
  }
  val musicRecTable = new TableView[MusicRecordItem](musicRecItems) {
    vgrow = Priority.ALWAYS
    hgrow = Priority.ALWAYS
    columns ++= List(durationColumn, trackColumn, albumColumn, artistColumn)
  }
  musicRecTable.selectionModel.value.setSelectionMode(SelectionMode.MULTIPLE)

  def initPlaylistMenuItem(i:CheckMenuItem, c:TableColumn[_,_]):Unit = {
    i.setSelected(true)
    i.setOnAction(
      new EventHandler[javafx.event.ActionEvent] {
        override def handle(event:javafx.event.ActionEvent) {
          event.consume()
          c.setVisible(i.isSelected)
        }
      })
  }
  val durationMenuItem = new CheckMenuItem("Duration")
  initPlaylistMenuItem(durationMenuItem, durationColumn)
  val trackMenuItem = new CheckMenuItem("Track")
  initPlaylistMenuItem(trackMenuItem, trackColumn)
  val albumMenuItem = new CheckMenuItem("Album")
  initPlaylistMenuItem(albumMenuItem, albumColumn)
  val artistMenuItem = new CheckMenuItem("Artist")
  initPlaylistMenuItem(artistMenuItem, artistColumn)
  val playListSettingsMnu:ContextMenu = new ContextMenu {
    style = "-fx-background-radius: 10 0 10 10; -fx-border-color: white; -fx-border-radius: 10 0 10 10;"
    autoHide  = true
    items ++= List(durationMenuItem, trackMenuItem, albumMenuItem, artistMenuItem)
  }
  val playlistSettingsBtn:Button = new Button {
    text = "..."
    prefHeight = 40
    prefWidth = 40
    onMouseClicked = new EventHandler[MouseEvent] {
      override def handle(event:MouseEvent) {
        event.consume()
        val topNode = parent.value.getScene.content.head
        if(!playListSettingsMnu.showing.value)
          playListSettingsMnu.show(topNode, Side.LEFT, event.getSceneX, event.getSceneY)
        else
          playListSettingsMnu.hide()
      }
    }
  }

  val deleteFilesBtn = new Button {
    text = "Delete"
    prefHeight = 40 // vgrow policy didn't work, set it directly for now
    vgrow = Priority.ALWAYS // Doesn't work?!?
    hgrow = Priority.ALWAYS
    onMouseClicked = new EventHandler[MouseEvent] {
      override def handle(event:MouseEvent) {
        event.consume()
        val smodel = musicRecTable.selectionModel.value
        smodel.getSelectedIndices.toList.sortWith((x, y) => x > y).foreach(musicRecItems.remove(_))
        smodel.clearSelection()
      }
    }
  }

  val openFilesBtn = new Button {
    text = "Open"
    vgrow = Priority.ALWAYS // Doesn't work?!?
    prefHeight = 40 // vgrow policy didn't work, set it directly for now
    onMouseClicked = new EventHandler[MouseEvent] {
      override def handle(event:MouseEvent) {
        event.consume()
        Try(fchooser.showOpenMultipleDialog(parent.value.getScene.getWindow)).map { fs =>
          fs.foreach { f => TrackMetaData(f) { md =>
            musicRecItems += new MusicRecordItem(md.artist, md.album, md.track, md.duration, f.getName)
          }}
        }
      }
    }
  }

  val playerStub = new Region {
    hgrow = Priority.ALWAYS
  }

  val playerControlsLayout = new HBox {
    hgrow = Priority.ALWAYS
    alignment = Pos.CENTER_LEFT
    minHeight = 40
    maxHeight = 40
    prefHeight = 40
    fillHeight = true // Doesn't work?!?
    content ++= List(openFilesBtn, deleteFilesBtn, playerStub, playlistSettingsBtn)
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
      width onChange {mainLayout.setPrefWidth(scene.value.getWidth);}
      height onChange {mainLayout.setPrefHeight(scene.value.getHeight);}
      content = mainLayout
      onCloseRequest = (event:WindowEvent) => {}
    }
  }
}
