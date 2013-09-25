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
  def apply(file: File)(onTrackMetaReady: TagMetaData => Unit):Unit = {
    val media = new Media(file.toURI.toString)
    val mediaPlayer = new MediaPlayer(media)
    mediaPlayer.onReady = {
      val metadata = media.metadata
      val tag = TagMetaData(
        artist = metadata.get(TagMetaData.ARTIST).asInstanceOf[String],
        track = metadata.get(TagMetaData.TRACK).asInstanceOf[String],
        album = metadata.get(TagMetaData.ALBUM).asInstanceOf[String]
      )
      onTrackMetaReady(tag)
    }
  }
}
