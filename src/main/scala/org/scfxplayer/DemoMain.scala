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

// *************** Some reading material **************** //
//http://search.maven.org/#search%7Cga%7C1%7Cscalafx
//http://raintomorrow.cc/post/50811498259/how-to-package-a-scala-project-into-an-executable-jar
//http://codingonthestaircase.wordpress.com/2013/09/11/what-is-new-in-scalafx-8-milestone-1/
//https://github.com/jugchennai/scalafx.g8/blob/master/src/main/g8/build.sbt
//http://java.dzone.com/articles/getting-started-scalafx-and

object DemoMain extends JFXApp {

  import scalafx.Includes._

  val files = List(new File("test-data/test.mp3"), new File("test-data/test.mp3"), new File("test-data/test.mp3"))

  def playPlaylist(pl:List[File]) {
    pl.headOption.map { f => if(f.exists) {
      val media = new Media(f.toURI.toURL.toExternalForm)
      val mplayer = new MediaPlayer(media)
      mplayer.onReady = onPlayerReady(mplayer)
      mplayer.onEndOfMedia = {
      }
    }}
  }

  val timePosSlider = new Slider {
    style = "-fx-padding: 0 10 0 10;"
    hgrow = Priority.ALWAYS
  }

  val playBtn:Button = new Button {
    text = "Play"
    prefWidth = 100
    onMouseClicked = (event:MouseEvent) => {
      event.consume()
      playPlaylist(files)
    }
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

//  val playerView = new MediaView {
//    mediaPlayer = mplayer
//    fitHeight = 40
//    fitWidth = 300
//    hgrow = Priority.ALWAYS
//    vgrow = Priority.ALWAYS
//    smooth = true
//    onError = (event: MediaErrorEvent) => println("Media view error: " + event)
//  }

  val playerControlsLayout = new HBox {
    prefHeight = 40
    minHeight = 40
    maxHeight = 40
    alignment = Pos.CENTER
    hgrow = Priority.ALWAYS
    vgrow = Priority.ALWAYS
    content = Seq(playBtn, timePosSlider)
  }

  val mainLayout = new VBox {
    content = Seq(playerControlsLayout)
  }

  stage = new PrimaryStage {
    title = "ScalaFX Demo Player"
    width = 650
    height = 100
    scene = new Scene {
      width onChange {mainLayout.setPrefWidth(scene.value.getWidth)}
      height onChange {mainLayout.setPrefHeight(scene.value.getHeight)}
      content = mainLayout
    }
    onShown = new EventHandler[WindowEvent] {
      override def handle(event:WindowEvent) {
        event.consume()
      }
    }
  }

}
