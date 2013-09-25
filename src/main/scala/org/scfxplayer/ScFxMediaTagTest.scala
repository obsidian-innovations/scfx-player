package org.scfxplayer

import java.io.File
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.media._
import scalafx.stage.FileChooser
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableColumn._
import scalafx.scene.control.{TableColumn, TableView, Button}
import scalafx.scene.layout.{HBox, Priority, VBox}
import scalafx.geometry.Pos
import scala.collection.JavaConversions._


class TMusicRecordItem( fileName_ : String,
                        artist_ : String,
                        track_ : String,
                        album_ : String) {
  val fileName = new StringProperty(this, "fileName", fileName_)
  val artist = new StringProperty(this, "artist", artist_)
  val track = new StringProperty(this, "track", track_)
  val album = new StringProperty(this, "album", album_)
}

object TMain extends JFXApp {

  val fchooser:FileChooser = new FileChooser()
  val f = new FileChooser.ExtensionFilter("MP3 (MPEG-1 or MPEG-2 Audio Layer III)", Seq("*.mp3", "*.MP3"))
  fchooser.getExtensionFilters.addAll(f)

  val musicRecItems = ObservableBuffer[TMusicRecordItem]()
  val musicRecTable = new TableView[TMusicRecordItem](musicRecItems) {
    vgrow = Priority.ALWAYS
    hgrow = Priority.ALWAYS
    columns ++= List(
      new TableColumn[TMusicRecordItem, String] {
        text = "Artist"
        cellValueFactory = {_.value.artist}
        prefWidth = 180
      },
      new TableColumn[TMusicRecordItem, String]() {
        text = "Track"
        cellValueFactory = {_.value.track}
        prefWidth = 180
      }
    )
  }

  val deleteFilesBtn = new Button {
    text = "Delete"
    prefHeight = 40
    vgrow = Priority.NEVER
  }

  val openFilesBtn = new Button {
    text = "Open"
    vgrow = Priority.ALWAYS // Doesn't work?!?
    managed = true
    prefHeight = 40 // vgrow policy didn't work, set it directly for now
    onMouseClicked = new EventHandler[MouseEvent] {
      override def handle(event:MouseEvent) {
        event.consume()
        fchooser.showOpenMultipleDialog(parent.value.getScene.getWindow) match {
          case null => Unit
          case fs => {
            musicRecItems.clear
            fs.map{ f => TrackMetaData(f){ md =>
              musicRecItems += new TMusicRecordItem(f.getName, md.artist,md.track,md.album)
            }}
          }
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
    content = Seq(openFilesBtn, deleteFilesBtn)
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
