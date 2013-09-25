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
import scala.collection.JavaConversions._

class MusicRecordItem(fileName_ : String, fileExt_ : String) {
  val fileName = new StringProperty(this, "fileName", fileName_)
  val fileExt = new StringProperty(this, "fileExt", fileExt_)
}

object Main extends JFXApp {

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
        text = "File Name"
        cellValueFactory = {_.value.fileName}
        prefWidth = 180
      },
      new TableColumn[MusicRecordItem, String]() {
        text = "File Extention"
        cellValueFactory = {_.value.fileExt}
        prefWidth = 180
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
          case fs => musicRecItems.addAll(fs.map(f => new MusicRecordItem(f.getName, "mp3")))
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
