package org.scfxplayer

import scalafx.beans.property.StringProperty
import org.joda.time.Duration
import org.joda.time.format.PeriodFormatterBuilder

import play.api.libs.json._
import scala.util.Try

case class MusicRecordItem(artist_ : Option[String],
                      album_ : Option[String],
                      track_ : Option[String],
                      duration_ : Duration,
                      fileName_ : String,
                      fullPath: String) {
  val artist = new StringProperty(this, "artist", artist_.getOrElse(""))
  val album = new StringProperty(this, "album", album_.getOrElse(""))
  val track = new StringProperty(this, "track", track_.getOrElse(""))
  val fileName = new StringProperty(this, "fileName", fileName_)
  val trackNameMade = new StringProperty(this, "trackNameMade", track_.getOrElse(cutExt(fileName_)))
  val duration = new StringProperty(this, "duration", durationToString(duration_))

  private var markedDeleted_ = false
  def markDeleted { markedDeleted_ = true }
  def isMarkedDeleted = markedDeleted_

  private def cutExt(s:String) = s.reverse.dropWhile(_ != '.').drop(1).reverse.toString
  private def durationToString(d:Duration):String = {
    val formatter = new PeriodFormatterBuilder()
      .appendHours()
      .appendSuffix(":")
      .appendMinutes()
      .appendSuffix(":")
      .appendSeconds()
      .appendSuffix("")
      .toFormatter()
    formatter.print(d.toPeriod())
  }

  def dndString: String = s"${artist.value}-${album.value}-${track.value}"
}
