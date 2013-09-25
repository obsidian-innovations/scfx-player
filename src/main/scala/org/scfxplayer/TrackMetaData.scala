package org.scfxplayer

import java.io.File
import scalafx.scene.media.{MediaPlayer, Media}

case class TagMetaData(artist:Option[String],track:Option[String],album:Option[String])
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
        artist = Option(mt.get(TagMetaData.ARTIST).asInstanceOf[String]),
        track = Option(mt.get(TagMetaData.TRACK).asInstanceOf[String]),
        album = Option(mt.get(TagMetaData.ALBUM).asInstanceOf[String])
      )
      f(tag)
    }
  }
}
