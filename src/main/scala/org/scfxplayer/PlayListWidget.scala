package org.scfxplayer

import scalafx.scene.control._
import scalafx.scene.layout.Priority
import scalafx.collections.ObservableBuffer
import scalafx.scene.input.{DragEvent, MouseEvent}
import javafx.scene.input.{ClipboardContent, TransferMode}
import javafx.scene.control.CheckMenuItem
import javafx.event.EventHandler

//http://blog.ngopal.com.np/2012/05/06/javafx-drag-and-drop-cell-in-listview/

class PlayListWidget(val musicRecItems:ObservableBuffer[MusicRecordItem]) {
  import scalafx.Includes._

  def attachDnDToColumn(column:TableColumn[MusicRecordItem, String]):TableColumn[MusicRecordItem, String] = {
    val oldFactory = column.delegate.cellFactoryProperty().getValue()
    column.cellFactory = f => {
      val cell:TableCell[MusicRecordItem,String] = oldFactory.call(f)
      cell.tableRow onChange {
        musicRecItems.lift(cell.tableRow.value.indexProperty.getValue).foreach { i =>
          cell.style <== when (i.playingNow) choose "-fx-font-weight: bold; -fx-text-fill: slateblue;" otherwise ""
        }
      }
      cell
    }
//    val oldFactory = column.delegate.cellFactoryProperty().getValue()
//    column.cellFactory = f => {
//      val res:TableCell[MusicRecordItem,String] = oldFactory.call(f)
//      res.onDragDropped = (event:DragEvent) => {
//        event.consume()
//        val db = event.getDragboard()
//        val success = if (event.getDragboard().hasString()) {
//          val droppedIdx = musicRecItems.indexWhere(_.fullPath == db.getString())
//          val targetIdx = res.tableRow.value.indexProperty().getValue
//          musicRecItems.find(_.fullPath == db.getString() && droppedIdx > -1 && targetIdx > -1).map { x =>
//            val newTrgtIdx:Int = if(targetIdx > droppedIdx) targetIdx + 1 else targetIdx
//            musicRecItems.insert(newTrgtIdx, x)
//            musicRecItems.remove(if(targetIdx > droppedIdx) droppedIdx else droppedIdx + 1)
//            column.getTableView.selectionModel.value.clearSelection()
//            column.getTableView.selectionModel.value.select(targetIdx)
//          }
//          db.clear()
//          true
//        } else false
//        event.setDropCompleted(success)
//      }
//      res
//    }
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

  private def isColumn(target: javafx.event.EventTarget):Boolean =
    target.isInstanceOf[com.sun.javafx.scene.control.skin.LabelSkin]

  private def isRow(target: javafx.event.EventTarget):Boolean = !isColumn(target)

  //https://forums.oracle.com/thread/2413845
  def setupDragAndDrop(tableView:TableView[MusicRecordItem]):TableView[MusicRecordItem] = {

    tableView.onDragDetected =  (event:MouseEvent) => {
      if(isRow(event.getTarget)){
        val selected = tableView.getSelectionModel().getSelectedItem();
        if(selected !=null){
          val db = tableView.startDragAndDrop(TransferMode.LINK)
          val content = new ClipboardContent();
          content.putString(selected.fullPath);
          db.setContent(content);
      }
    }}

    tableView.onDragOver =  (event:DragEvent) => {
      if(isRow(event.getTarget)){
        event.consume()
        val db = event.getDragboard()
        if (event.getDragboard().hasString()) {
          event.acceptTransferModes(TransferMode.LINK)
        }
      }
    }

    tableView
  }

  def tableView(onItemDblClicked: MusicRecordItem => Unit):TableView[MusicRecordItem] = {
    val table = new TableView[MusicRecordItem](musicRecItems) {
      vgrow = Priority.ALWAYS
      hgrow = Priority.ALWAYS
      columns ++= List(durationColumn, trackColumn, albumColumn, artistColumn)
    }
    table.selectionModel.value.setSelectionMode(SelectionMode.MULTIPLE)
    table.onMouseClicked = (event:MouseEvent) => {
      if(event.getClickCount() == 2)
        Option(table.getSelectionModel().getSelectedItem()).map(onItemDblClicked(_))
    }
    //setupDragAndDrop(table, onItemDblClicked)
    table
  }
}