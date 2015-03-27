package org.scfxplayer.gui

import scalafx.scene.control._
import scalafx.scene.layout.Priority
import scalafx.collections.ObservableBuffer
import scalafx.scene.input.{DataFormat, DragEvent, MouseEvent}
import javafx.scene.input.{ClipboardContent, TransferMode}
import javafx.scene.control.CheckMenuItem
import javafx.event.EventHandler
import scalafx.scene.{SnapshotParameters, ImageCursor, Cursor, Node}
import com.sun.javafx.scene.control.skin.LabelSkin
import scalafx.scene.paint.Color
import scalafx.scene.text.Text
import scala.util.Try
import org.scfxplayer.controller.PlayListController
import org.scfxplayer.model.MusicRecordItem

//http://blog.ngopal.com.np/2012/05/06/javafx-drag-and-drop-cell-in-listview/

class PlayListWidget(val pmgr:PlayListController) {
  import scalafx.Includes._

  private def rebindCellStyle(cell:TableCell[MusicRecordItem,String]):Unit = {
    for(
      i <- Try(cell.tableRow.value.getItem.asInstanceOf[MusicRecordItem]).filter(_ != null)
    ) {
      cell.style.unbind()
      cell.style <== when (i.playingNow) choose {
        "-fx-font-weight: bold; -fx-text-fill: slateblue;"
      } otherwise {
        ""
      }
    }
  }

  private def setOnPlayingItem(cell:TableCell[MusicRecordItem,String]):Unit = {
    cell.tableRow onChange {
      (x, oldtab, newtab) => {
        rebindCellStyle(cell)
        cell.tableRow.value.item onChange {
          (x, olditem, newitem) => {
            rebindCellStyle(cell)
          }
        }
      }
    }
  }

  private def setOnDragDropped(cell:TableCell[MusicRecordItem,String], column:TableColumn[MusicRecordItem, String]):Unit = {
    cell.onDragDropped = (event:DragEvent) => {
      event.consume()
      val db = event.getDragboard()
      if(event.getSource != cell && event.getDragboard().hasString()) {
        val droppedIdx = pmgr.musicRecItems.indexWhere(_.fullPath == db.getString())
        val targetIdx = cell.tableRow.value.indexProperty().getValue
        pmgr.musicRecItems.find(_.fullPath == db.getString() && droppedIdx > -1 && targetIdx > -1).map { x =>
          val newTrgtIdx:Int = if(targetIdx > droppedIdx) targetIdx + 1 else targetIdx
          pmgr.musicRecItems.insert(newTrgtIdx, x)
          pmgr.musicRecItems.remove(if(targetIdx > droppedIdx) droppedIdx else droppedIdx + 1)
          column.getTableView.selectionModel.value.clearSelection()
          column.getTableView.selectionModel.value.select(targetIdx)
          pmgr.saveCurrentPlaylist()
        }
        db.clear()
      }
      event.setDropCompleted(true)
    }

    cell.onDragDetected = (event:MouseEvent) => {
      event.consume()
      val db = cell.startDragAndDrop(TransferMode.MOVE)
      val content = new ClipboardContent()
      content.putString(cell.tableRow.value.getItem.asInstanceOf[MusicRecordItem].fullPath)
      db.setContent(content)
    }

    cell.onDragOver = (event:DragEvent) => {
      event.consume()
      if(event.getGestureSource != cell && event.getDragboard().hasString()) {
        event.acceptTransferModes(TransferMode.MOVE)
      }
    }

    cell.onDragEntered = (event:DragEvent) => {
      event.consume()
      if(event.getGestureSource() != cell && event.getDragboard().hasString()) {
        cell.tableRow.value.setOpacity(0.7)
      }
    }

    cell.onDragExited = (event:DragEvent) => {
      event.consume()
      cell.tableRow.value.setOpacity(1.0)
    }

  }

  private def attachDnDToColumn(column:TableColumn[MusicRecordItem, String]):TableColumn[MusicRecordItem, String] = {
    val oldFactory = column.delegate.cellFactoryProperty().getValue()
    column.cellFactory = f => {
      val cell:TableCell[MusicRecordItem,String] = oldFactory.call(f)
      setOnDragDropped(cell, column)
      setOnPlayingItem(cell)
      cell
    }
    column
  }

  val durationColumn = attachDnDToColumn(new TableColumn[MusicRecordItem, String]() {
    text = "Duration"
    prefWidth = 50
    minWidth = 50
    cellValueFactory = {_.value.duration}
  })

  val trackColumn = attachDnDToColumn(new TableColumn[MusicRecordItem, String] {
    text = "Track"
    prefWidth = 230
    minWidth = 50
    cellValueFactory = {_.value.trackNameMade}
  })

  val albumColumn = attachDnDToColumn(new TableColumn[MusicRecordItem, String]() {
    text = "Album"
    prefWidth = 58
    minWidth = 50
    cellValueFactory = {_.value.album}
  })

  val artistColumn = attachDnDToColumn(new TableColumn[MusicRecordItem, String]() {
    text = "Artist"
    prefWidth = 58
    minWidth = 50
    cellValueFactory = {_.value.artist}
  })

  val durationMenuItem = new CheckMenuItem("Duration")
  initPlaylistMenuItem(durationMenuItem, durationColumn)
  val trackMenuItem = new CheckMenuItem("Track")
  initPlaylistMenuItem(trackMenuItem, trackColumn)
  val albumMenuItem = new CheckMenuItem("Album")
  initPlaylistMenuItem(albumMenuItem, albumColumn)
  val artistMenuItem = new CheckMenuItem("Artist")
  initPlaylistMenuItem(artistMenuItem, artistColumn)

  val playListSettingsMnu:ContextMenu = new ContextMenu {
    styleClass ++= Seq("settings-menu")
    autoHide  = true
    items ++= Seq(durationMenuItem, trackMenuItem, albumMenuItem, artistMenuItem)
  }

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

  def tableView(onItemDblClicked: MusicRecordItem => Unit):TableView[MusicRecordItem] = {
    val table = new TableView[MusicRecordItem](pmgr.musicRecItems) {
      vgrow = Priority.ALWAYS
      hgrow = Priority.ALWAYS
      columns ++= List(durationColumn, trackColumn, albumColumn, artistColumn)
    }
    table.selectionModel.value.setSelectionMode(SelectionMode.MULTIPLE)
    table.onMouseClicked = (event:MouseEvent) => {
      if(event.getClickCount() == 2)
        Option(table.getSelectionModel().getSelectedItem()).map(onItemDblClicked(_))
    }
    table
  }
}