package org.scfxplayer

import java.io.File
import scalafx.scene.media.{MediaPlayer, Media}
import javafx.util.Duration

case class TagMetaData(artist:Option[String],
                       track:Option[String],
                       album:Option[String],
                       duration:Duration)

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
        artist = Option(metadata.get(TagMetaData.ARTIST).asInstanceOf[String]),
        track = Option(metadata.get(TagMetaData.TRACK).asInstanceOf[String]),
        album = Option(metadata.get(TagMetaData.ALBUM).asInstanceOf[String]),
        duration = media.getDuration
      )
      onTrackMetaReady(tag)
    }
  }
}
