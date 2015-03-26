package org.scfxplayer.gui

import scalafx.scene.layout._
import scalafx.collections.ObservableBuffer
import org.scfxplayer.controller.PlayListController
import scalafx.geometry.{Side, Pos}
import scalafx.scene.control.Button
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import org.scfxplayer.model.MusicRecordItem
import scalafx.stage
import scalafx.stage.Stage
import scala.util.Try

class PlayerView(stage: =>Stage) extends VBox {
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

  val plMgr = new PlayListController(musicRecItems)
  val playList = new PlayListWidget(plMgr)
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
        smodel.getSelectedIndices.toList.distinct.sortWith((x, y) => x > y).foreach { x =>
          musicRecItems.lift(x).foreach(_.markDeleted)
          musicRecItems.remove(x)
        }
        plMgr.saveCurrentPlaylist()
        smodel.clearSelection()
      }
    }
  }

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
    //content = Seq(lspacer, playerControls, rspacer)
    children = Seq(lspacer, playerControls, rspacer)
  }

  val otherControlsLayout = new HBox {
    val spacer = new Region {hgrow = Priority.ALWAYS; pickOnBounds = false}
    pickOnBounds = false
    hgrow = Priority.ALWAYS
    vgrow = Priority.ALWAYS
    alignment = Pos.BOTTOM_CENTER
    spacing = 6
//    content = Seq(openFilesBtn, spacer, playlistSettingsBtn, deleteFilesBtn)
    children = Seq(openFilesBtn, spacer, playlistSettingsBtn, deleteFilesBtn)
  }

  val mainControlsLayout = new StackPane {
    styleClass ++= Seq("player-controls-bg")
    hgrow = Priority.ALWAYS
    minHeight = 90
    maxHeight = 90
//    content = Seq(playerControlsLayout, otherControlsLayout)
    children = Seq(playerControlsLayout, otherControlsLayout)
  }

  plMgr.currentPlayListName.onChange {
    (_, changes) => {
      for (change <- changes) change match {
        case ObservableBuffer.Add(position, els) => {
          stage.setTitle(s"Demo ScalaFX Player - ${els.headOption.map(_.toString).getOrElse("")}")
        }
        case _ => {}
      }
    }
  }

  def onWidthUpdated(oldw:Double, neww:Double) = {
    val oldWidth = math.max(oldw, stage.minWidth)
    val newWidth = math.max(neww, stage.minWidth)
    musicRecTable.columns.foreach {
      c => c.setPrefWidth(Try(c.getWidth * newWidth / oldWidth).getOrElse(0.0))
    }
  }

  styleClass ++= Seq("root")
  vgrow = Priority.ALWAYS
  hgrow = Priority.ALWAYS
//  content = Seq(mainControlsLayout, musicRecTable)
  children = Seq(mainControlsLayout, musicRecTable)

}
