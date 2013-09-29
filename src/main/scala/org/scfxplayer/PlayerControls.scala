package org.scfxplayer

import java.io.File
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.layout.{VBox, Priority, HBox}
import javafx.event.EventHandler
import javafx.stage.WindowEvent
import scalafx.scene.control.{Slider, Button}
import scalafx.scene.media.{Media, MediaPlayer, MediaView, MediaErrorEvent}
import scalafx.scene.input.MouseEvent
import scalafx.util.Duration
import scalafx.geometry.Pos
import scala.util.Try
import scalafx.event
import scala.annotation.tailrec
import scalafx.collections.ObservableBuffer

class PlayerControls(items:ObservableBuffer[MusicRecordItem]) extends HBox {
  import scalafx.Includes._

  private val timePosSlider = new Slider {
    style = "-fx-padding: 0 10 0 10;"
    hgrow = Priority.ALWAYS
  }

  private val playBtn:Button = new Button {
    prefWidth = 40
  }

  private val nextBtn:Button = new Button {
    prefWidth = 40
    text = ">"
    onMouseClicked = (event:MouseEvent) => {
      event.consume()
      playing.foreach(scheduleNextPlay(_))
    }
  }

  private val prevBtn:Button = new Button {
    prefWidth = 40
    text = "<"
    onMouseClicked = (event:MouseEvent) => {
      event.consume()
      playing.foreach(schedulePrevPlay(_))
    }
  }

  private var stop_ = () => {}
  private var playing_ : () => Option[MusicRecordItem] = () => None
  private def initState() {
    stop_ = () => {}
    playing_ = () => None
    timePosSlider.value onChange {}
    playBtn.onMouseClicked = onPlayClickedInit()_
    playBtn.text = "Play"
  }

  def stop() = { stop_(); initState() }
  def playing = playing_()
  def isPlaying(i:MusicRecordItem) = playing.map(_.fullPath == i.fullPath).getOrElse(false)
  def play(item:MusicRecordItem) { play(Some(item)) }

  def play(item:Option[MusicRecordItem]) {
    stop()
    (item.headOption orElse items.headOption).map {  r =>
      (for {
        file <- Try(new File(r.fullPath))
        media <- Try(new Media(file.toURI.toURL.toExternalForm))
        mplayer <- Try(new MediaPlayer(media))
      } yield {
        stop_ = () => mplayer.stop()
        playing_ = () => Some(r)
        mplayer.onReady = onPlayerReady(mplayer)
        mplayer.onEndOfMedia = scheduleNextPlay(r)
        mplayer.onError = scheduleNextPlay(r)
        mplayer.onStalled = scheduleNextPlay(r)
      }).getOrElse(scheduleNextPlay(r))
    }
  }

  private def scheduleNextPlay(played:MusicRecordItem) {
    play(items.dropWhile {_.fullPath != played.fullPath}.drop(1).headOption)
  }

  private def schedulePrevPlay(played:MusicRecordItem) {
    play(items.reverse.dropWhile {_.fullPath != played.fullPath}.drop(1).headOption)
  }

  private def isPlaying(p:MediaPlayer) = p.status.value.toString == MediaPlayer.Status.PLAYING.toString()

  private def onPlayClickedInit()(event:MouseEvent) = {
    event.consume()
    play(items.headOption)
  }

  private def onPlayClicked(player:MediaPlayer)(event:MouseEvent) = {
    event.consume()
    if(isPlaying(player)) player.pause()
    else player.play()
  }

  private def onPlayerReady(mplayer:MediaPlayer) = {
    mplayer.onPlaying = {playBtn.text = "Pause"}
    mplayer.onPaused = {playBtn.text = "Play"}
    playBtn.onMouseClicked = onPlayClicked(mplayer)_
    timePosSlider.min = 0.0
    timePosSlider.max = mplayer.media.duration.value.toSeconds
    timePosSlider.value = 0.0
    timePosSlider.value onChange {
      if(!timePosSlider.valueChanging.value || !isPlaying(mplayer))
        mplayer.seek(Duration(timePosSlider.value.value * 1000.0))
    }
    mplayer.currentTime onChange {
      timePosSlider.valueChanging = true
      Try(timePosSlider.value = mplayer.currentTime.value.toSeconds)
      timePosSlider.valueChanging = false
    }
    mplayer.play()
  }

  content = Seq(prevBtn, playBtn, nextBtn, timePosSlider)
  initState()
}
