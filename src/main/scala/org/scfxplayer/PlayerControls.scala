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

  val timePosSlider = new Slider {
    style = "-fx-padding: 0 10 0 10;"
    hgrow = Priority.ALWAYS
  }

  val playBtn:Button = new Button {
    text = "Play"
    prefWidth = 100
    onMouseClicked = (event:MouseEvent) => {
      event.consume()
      playPlaylist(items.headOption, items)
    }
  }

  def playPlaylist(toPlay:Option[MusicRecordItem], pl:ObservableBuffer[MusicRecordItem]) {
    (toPlay.headOption orElse pl.headOption).map {  r =>
      (for {
        file <- Try(new File(r.fullPath))
        media <- Try(new Media(file.toURI.toURL.toExternalForm))
        mplayer <- Try(new MediaPlayer(media))
      } yield {
        mplayer.onReady = onPlayerReady(mplayer)
        mplayer.onEndOfMedia = { mplayer.stop(); scheduleNextPlay(r, pl) }
        mplayer.onError = { mplayer.stop(); scheduleNextPlay(r, pl) }
        mplayer.onStalled = { mplayer.stop(); scheduleNextPlay(r, pl) }
      }).getOrElse(scheduleNextPlay(r, pl))
    }
  }

  def scheduleNextPlay(played:MusicRecordItem, pl:ObservableBuffer[MusicRecordItem]) {
    val nextToPlay = pl.dropWhile {_.fullPath != played.fullPath}.drop(1).headOption
    playPlaylist(nextToPlay, pl)
  }

  def isPlaying(p:MediaPlayer) = p.status.value.toString == MediaPlayer.Status.PLAYING.toString()

  def onPlayClicked(player:MediaPlayer)(event:MouseEvent) = {
    event.consume()
    if(isPlaying(player)) player.pause()
    else player.play()
  }

  def onPlayerReady(mplayer:MediaPlayer) = {
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

  content = Seq(playBtn, timePosSlider)

}
