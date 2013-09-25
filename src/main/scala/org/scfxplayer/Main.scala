package org.scfxplayer

import java.io.File
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.stage.FileChooser
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableColumn._
import scalafx.scene.control.{SelectionMode, TableColumn, TableView, Button}
import scalafx.scene.layout.{Priority, HBox, VBox}
import scalafx.geometry.Pos
import scalafx.scene.media.{Media, MediaPlayer, MediaView, MediaErrorEvent}
import scala.collection.JavaConversions._



object Main extends JFXApp {

  import scalafx.Includes._

  val testMp3 = new File("test-data/test.mp3")
  val media = new Media(testMp3.toURI.toURL.toExternalForm)
  val mplayer = new MediaPlayer(media)
  val playerView = new MediaView {
    mediaPlayer = mplayer
    fitHeight = 40
    fitWidth = 300
    hgrow = Priority.ALWAYS
    vgrow = Priority.ALWAYS
    smooth = true
    onError = (event: MediaErrorEvent) => println("Media view error: " + event)
  }

  val fchooser:FileChooser = new FileChooser()
  val f = new FileChooser.ExtensionFilter("MP3 (MPEG-1 or MPEG-2 Audio Layer III)", Seq("*.mp3", "*.MP3"))
  fchooser.getExtensionFilters.addAll(f)
  fchooser.setTitle("Demo ScalaFX Player")
  fchooser.setInitialDirectory(new File(System.getProperty("user.home")))

  val musicRecItems = ObservableBuffer[MusicRecordItem]()
  val musicRecTable = new TableView[MusicRecordItem](musicRecItems) {
    vgrow = Priority.ALWAYS
    hgrow = Priority.ALWAYS
    columns ++= List(
      new TableColumn[MusicRecordItem, String] {
        text = "Track"
        cellValueFactory = {_.value.trackNameMade}
      },
      new TableColumn[MusicRecordItem, String]() {
        text = "Album"
        cellValueFactory = {_.value.album}
      },
      new TableColumn[MusicRecordItem, String]() {
        text = "Artist"
        cellValueFactory = {_.value.artist}
      },
      new TableColumn[MusicRecordItem, String]() {
        text = "Duration"
        cellValueFactory = {_.value.duration}
      }
    )
  }
  musicRecTable.selectionModel.value.setSelectionMode(SelectionMode.MULTIPLE)

  val deleteFilesBtn = new Button {
    text = "Delete"
    prefHeight = 40 // vgrow policy didn't work, set it directly for now
    vgrow = Priority.ALWAYS // Doesn't work?!?
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
        fchooser.showOpenMultipleDialog(parent.value.getScene.getWindow) match {
          case null => Unit
          case fs => { fs.foreach { f => TrackMetaData(f) { md =>
                musicRecItems += new MusicRecordItem(md.artist, md.album, md.track, md.duration, f.getName)
          }}}
        }
      }
    }
  }

  val playerControlsLayout = new HBox {
    hgrow = Priority.ALWAYS
    alignment = Pos.CENTER_LEFT
    minHeight = 40
    maxHeight = 40
    prefHeight = 40
    fillHeight = true // Doesn't work?!?
    content = Seq(openFilesBtn, deleteFilesBtn, playerView)
  }

  val mainLayout = new VBox {
    vgrow = Priority.ALWAYS
    hgrow = Priority.ALWAYS
    content = Seq(playerControlsLayout, musicRecTable)
  }

  stage = new PrimaryStage {
    title = "Demo ScalaFX Player"
    width = 640
    height = 480
    scene = new Scene {
      width onChange {mainLayout.setPrefWidth(scene.value.getWidth);}
      height onChange {mainLayout.setPrefHeight(scene.value.getHeight);}
      content = mainLayout
    }
  }
}
