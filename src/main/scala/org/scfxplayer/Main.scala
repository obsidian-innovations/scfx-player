package org.scfxplayer

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.{TableView, Button}
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import scalafx.stage.FileChooser
import java.io.File
import scala.collection.JavaConversions._
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableColumn._
import scalafx.scene.control.{TableColumn, TableView}
import scalafx.scene.layout.{Priority, VBox}

class MusicRecordItem(fileName_ : String, fileExt_ : String) {
  val fileName = new StringProperty(this, "fileName", fileName_)
  val fileExt = new StringProperty(this, "fileExt", fileExt_)
}

object Main extends JFXApp {

  val fchooser:FileChooser = new FileChooser()
  val f = new FileChooser.ExtensionFilter("MP3 (MPEG-1 or MPEG-2 Audio Layer III)", Seq("*.mp3", "*.MP3"))
  fchooser.getExtensionFilters.addAll(f)

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

  val mainLayout = new VBox {
    vgrow = Priority.ALWAYS
    hgrow = Priority.ALWAYS
    content = Seq(
      new Button {
        text = "Open"
        onMouseClicked = new EventHandler[MouseEvent] {
          override def handle(event:MouseEvent) {
            event.consume()
            fchooser.showOpenMultipleDialog(parent.value.getScene.getWindow) match {
              case null => Unit
              case fs => {
                musicRecItems.clear
                musicRecItems.addAll(fs.map(f => new MusicRecordItem(f.getName, "mp3")))
              }
            }
          }
        }
      },
      musicRecTable
    )
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
