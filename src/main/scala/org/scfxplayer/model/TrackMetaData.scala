package org.scfxplayer.model

import java.io.File
import scalafx.scene.media.{MediaPlayer, Media}
import org.joda.time.Duration
import scala.util.{Failure, Success, Try}

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
  def apply(file: File)(onTrackMetaReady: Try[TagMetaData] => Unit):Unit = {
    if(file.exists()) {
      val media = new Media(file.toURI.toString)
      val mediaPlayer = new MediaPlayer(media)
      mediaPlayer.onReady = {
        val metadata = media.metadata
        val tag = TagMetaData(
          artist = Option(metadata.get(TagMetaData.ARTIST).asInstanceOf[String]),
          track = Option(metadata.get(TagMetaData.TRACK).asInstanceOf[String]),
          album = Option(metadata.get(TagMetaData.ALBUM).asInstanceOf[String]),
          duration = new Duration(media.getDuration.toMillis.toLong)
        )
        onTrackMetaReady(Success(tag))
      }
    } else onTrackMetaReady(Failure(new java.io.FileNotFoundException(s"${file.getAbsolutePath} is not found !")))
  }
}
