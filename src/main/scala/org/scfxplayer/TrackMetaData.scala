package org.scfxplayer

import java.io.File
import scalafx.scene.media.{MediaPlayer, Media}

case class TagMetaData(artist:String,track:String,album:String)
object TagMetaData {
  val ARTIST = "artist"
  val TRACK = "title"
  val ALBUM = "album"
}

object TrackMetaData {
  def apply(file: File)(f: TagMetaData => Unit):Unit = {
    val m = new Media(file.toURI.toString)
    val mediaPlayer = new MediaPlayer(m)

    mediaPlayer.onReady = {
      val mt = m.metadata
      println(mt)
      val tag = TagMetaData(
        artist = mt.get(TagMetaData.ARTIST).asInstanceOf[String],
        track = mt.get(TagMetaData.TRACK).asInstanceOf[String],
        album = mt.get(TagMetaData.ALBUM).asInstanceOf[String]
      )
      f(tag)
    }
  }
}
