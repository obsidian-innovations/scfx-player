package org.scfxplayer

import java.io.File
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.stage.{FileChooser, WindowEvent}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Side, Pos}
import scalafx.scene.image.Image
import scala.util.Try

object Main extends JFXApp {
  import scalafx.Includes._

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
    prefWidth = 24
    prefHeight = 24
    onMouseClicked = new EventHandler[MouseEvent] {
      override def handle(event:MouseEvent) {
        event.consume()
        if(!playList.playListSettingsMnu.showing.value)
          playList.playListSettingsMnu.show(playlistSettingsBtn, Side.LEFT, 0, playlistSettingsBtn.height.value)
        else
          playList.playListSettingsMnu.hide()
      }
    }
  }

  val deleteFilesBtn = new Button {
    styleClass ++= List("player-button", "button-delete")
    prefWidth = 24
    prefHeight = 24
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

  val plMgr = new PlayListManagerMenu(musicRecItems)

  lazy val openFilesBtn:Button = new Button {
      styleClass ++= List("player-button", "button-eject")
      prefWidth = 24
      prefHeight = 24
      onMouseClicked = new EventHandler[MouseEvent] {
        override def handle(event:MouseEvent) {
          event.consume()
          val plMgrMenu = plMgr.menu(parent.value)
          if(!plMgrMenu.showing.value)
            plMgrMenu.show(openFilesBtn, Side.RIGHT, 0, openFilesBtn.height.value)
          else
            plMgrMenu.hide()
        }
      }
    }

  val playerControlsLayout = new HBox {
    val lspacer = new Region {hgrow = Priority.SOMETIMES}
    val rspacer = new Region {hgrow = Priority.SOMETIMES}
    hgrow = Priority.ALWAYS
    vgrow = Priority.ALWAYS
    content = Seq(lspacer, playerControls, rspacer)
  }

  val otherControlsLayout = new HBox {
    val spacer = new Region {hgrow = Priority.ALWAYS; pickOnBounds = false}
    pickOnBounds = false
    hgrow = Priority.ALWAYS
    vgrow = Priority.ALWAYS
    alignment = Pos.BOTTOM_CENTER
    spacing = 6
    content = Seq(openFilesBtn, spacer, playlistSettingsBtn, deleteFilesBtn)
  }

  val mainControlsLayout = new StackPane {
    styleClass ++= Seq("player-controls-bg")
    hgrow = Priority.ALWAYS
    minHeight = 90
    maxHeight = 90
    content = Seq(playerControlsLayout, otherControlsLayout)
  }

  val mainLayout = new VBox {
    styleClass ++= Seq("root")
    vgrow = Priority.ALWAYS
    hgrow = Priority.ALWAYS
    content = Seq(mainControlsLayout, musicRecTable)
  }

  plMgr.currentPlayListName.onChange {
    (_, changes) => {
      for (change <- changes) change match {
        case ObservableBuffer.Add(position, els) => {
          stage.title = s"Demo ScalaFX Player - ${els.headOption.map(_.toString).getOrElse("")}"
        }

        case _ => {}
      }
    }
  }

  stage = new PrimaryStage {
    title = "Demo ScalaFX Player"
    icons ++= Seq(new Image(getClass.getResource("/app-icon-32.png").toExternalForm))
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
        PlayListManager.saveCurrent(pl)
      }
      onShowing = (event:WindowEvent) => { plMgr.loadDefaultPlaylist() }
    }
  }

}
