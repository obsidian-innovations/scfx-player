package org.scfxplayer

import scalafx.application.JFXApp
import scalafx.scene.media.{MediaPlayer, Media}
import java.io.File


object ScFxMediaTagTest extends JFXApp {

  val fname = "test-data/test.mp3"
  val file = new File(fname)

  val m = new Media(file.toURI.toString)
  val mediaPlayer = new MediaPlayer(m)

  mediaPlayer.onReady = {
    println(mediaPlayer.status)
    println(m.source)
    println(m.tracks)
    println(m.getDuration)
    println(m.getMetadata)
  }

}