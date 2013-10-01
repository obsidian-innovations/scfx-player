package org.scfxplayer

import java.io.File
import scalafx.scene.layout.{VBox, Priority, HBox}
import scalafx.scene.control.{Slider, Button}
import scalafx.scene.media.{Media, MediaPlayer}
import scalafx.scene.input.MouseEvent
import scalafx.util.Duration
import scalafx.geometry.Pos
import scala.util.Try
import scalafx.collections.ObservableBuffer
import scalafx.scene.text.Text
import scalafx.animation._
import scala.Some
import scalafx.scene.shape.Rectangle
//import scalafx.scene.image.{ImageView, Image}

class PlayerControls(items:ObservableBuffer[MusicRecordItem]) extends VBox {
  import scalafx.Includes._

  private val timePosSlider = new Slider {
    hgrow = Priority.ALWAYS
    vgrow = Priority.ALWAYS
  }

  private val volumeSlider:Slider = new Slider {
    style = "-fx-padding: 0 0 0 10;"
    hgrow = Priority.NEVER
    vgrow = Priority.ALWAYS
    maxWidth = 90
    minWidth = 90
    min = 0.0
    max = 1.0
    opacity = 0.75
    value = 0.75
    value onChange {
      (s, oldVal, newVal) => {
        volumeSlider.opacity = newVal.doubleValue.max(0.3)
        player_().foreach { p =>
          p.volume = newVal.doubleValue
        }
      }
    }
  }

  private val playingNowText:Text = new Text {
    styleClass ++= Seq("playing-now-indicator")
    layoutBounds onChange {
      (t, oldVal, newVal) => {
        updateScroller(-1.0 * newVal.getWidth, scroller.fromX.value, scroller.duration.value)
      }
    }
  }

  private val playBtn:Button = new Button {
    styleClass ++= List("player-button", "button-play")
    maxWidth = 32
    minWidth = 32
    maxHeight = 32
    minHeight = 32
  }

  private val nextBtn:Button = new Button {
    styleClass ++= List("player-button", "button-forward")
    maxWidth = 32
    minWidth = 32
    maxHeight = 32
    minHeight = 32
//    graphic = new ImageView {
//      image = new Image(getClass.getResource("/actions-media-skip-forward-icon-32.png").toExternalForm)
//    }
    onMouseClicked = (event:MouseEvent) => {
      event.consume()
      playing.foreach(scheduleNextPlay(_))
    }
  }

  private val prevBtn:Button = new Button {
    styleClass ++= List("player-button", "button-backward")
    maxWidth = 32
    minWidth = 32
    maxHeight = 32
    minHeight = 32
    onMouseClicked = (event:MouseEvent) => {
      event.consume()
      playing.foreach(schedulePrevPlay(_))
    }
  }

  private val timeMark = new Text {

    styleClass ++= Seq("time-left-text-indicator")
    managed = false
    translateY = 23
    layoutBounds onChange updateTimeLeftIdk
  }

  private val timePosLayout:VBox = new VBox {
    hgrow = Priority.ALWAYS
    vgrow = Priority.NEVER
    alignment = Pos.BOTTOM_CENTER
    content = Seq(timePosSlider, timeMark)
    width onChange updateTimeLeftIdk
  }

  private lazy val playingTextLayout:HBox = new HBox {
    alignment = Pos.CENTER_LEFT
    vgrow = Priority.ALWAYS
    hgrow = Priority.ALWAYS
    content = Seq(playingNowText)
    width onChange {
      (s, oldVal, newVal) => {
        playingTextLayout.clip = new Rectangle {width = newVal.doubleValue; height = playingTextLayout.height.value}
        val playFrom = Try(scroller.currentTime.value.toMillis * newVal.doubleValue / oldVal.doubleValue).getOrElse(0.0)
        updateScroller(scroller.toX.value, newVal.doubleValue, Duration(if(playFrom.isNaN) 0.0 else playFrom))
      }
    }
    height onChange {
      (s, oldv, newv) => {
        playingTextLayout.clip = new Rectangle {width = playingTextLayout.width.value; height = newv.doubleValue}
      }
    }
  }

  private lazy val scroller = new TranslateTransition {
    cycleCount = Timeline.INDEFINITE
    autoReverse = false
    interpolator = Interpolator.LINEAR
    fromX = 0.0
    toX = 0.0
    duration = Duration(0.0)
    node = playingNowText
  }

  private def updateTimeLeftIdk {
    timeMark.translateX = timePosLayout.width.value - timeMark.layoutBounds.value.getWidth - 5
  }

  private def updateScroller(moveto:Double, movefrom:Double, playfrom:Duration) {
    scroller.stop()
    scroller.fromX = movefrom
    scroller.toX = moveto
    scroller.duration = Duration(30.0 * movefrom.abs)
    scroller.playFrom(playfrom)
  }

  private val btnsLayout = new HBox {
    style = "-fx-padding: 5 0 5 0;"
    alignment = Pos.CENTER
    spacing = 15
    content = Seq(prevBtn, playBtn, nextBtn, volumeSlider)
  }

  content = Seq(playingTextLayout, timePosLayout, btnsLayout)

  private var player_ : () => Option[MediaPlayer] = () => None
  private var playing_ : () => Option[MusicRecordItem] = () => None
  private def initState() {
    player_().foreach(_.stop())
    player_ = () => None
    playing_ = () => None
    timePosSlider.value onChange {}
    playBtn.onMouseClicked = onPlayClickedInit()_
    playBtn.styleClass -= "button-play-paused"
    scroller.stop()
    playingNowText.text = ""
    timeMark.text = ""
  }

  def stop() = initState()
  def playing = playing_()
  def isPlaying(i:MusicRecordItem) = playing.map(_.fullPath == i.fullPath).getOrElse(false)
  def play(item:MusicRecordItem) { play(Some(item)) }

  def play(item:Option[MusicRecordItem]) {
    initState()
    (item.headOption orElse items.headOption).map {  r =>
      (for {
        file <- Try(new File(r.fullPath))
        media <- Try(new Media(file.toURI.toURL.toExternalForm))
        mplayer <- Try(new MediaPlayer(media))
      } yield {
        player_ = () => Some(mplayer)
        playing_ = () => Some(r)
        mplayer.onReady = onPlayerReady(mplayer)
        mplayer.onEndOfMedia = scheduleNextPlay(r)
        mplayer.onError = scheduleNextPlay(r)
        mplayer.onStalled = scheduleNextPlay(r)
        playingNowText.text = r.trackNameMade.value
        scroller.playFromStart()
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

  private def updateTimeLeftIndicator() {
    player_().foreach { p =>
      val timeleft = (p.totalDuration.value - p.currentTime.value).toMillis.toLong
      timeMark.text = "-" + PlayerUtils.millisToString(timeleft)
    }
  }

  private def onPlayerReady(mplayer:MediaPlayer) = {
    mplayer.volume = volumeSlider.value.value
    mplayer.onPlaying = {playBtn.styleClass += "button-play-paused"; ()}
    mplayer.onPaused = {playBtn.styleClass -= "button-play-paused"; ()}
    playBtn.onMouseClicked = onPlayClicked(mplayer)_
    timePosSlider.min = 0.0
    timePosSlider.max = mplayer.media.duration.value.toSeconds
    timePosSlider.value = 0.0
    timePosSlider.value onChange {
      if(!timePosSlider.valueChanging.value || !isPlaying(mplayer)) {
        mplayer.mute = true
        Try(mplayer.seek(Duration(timePosSlider.value.value * 1000.0))).foreach { _ =>
          updateTimeLeftIndicator()
        }
        mplayer.mute = false
      }
    }
    mplayer.currentTime onChange {
      timePosSlider.valueChanging = true
      Try(timePosSlider.value = mplayer.currentTime.value.toSeconds).foreach { _ =>
        updateTimeLeftIndicator()
      }
      timePosSlider.valueChanging = false
    }
    mplayer.play()
  }

  initState()
}
