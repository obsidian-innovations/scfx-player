package org.scfxplayer

import scalafx.beans.property.{BooleanProperty, StringProperty}
import org.joda.time.Duration

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
  val duration = new StringProperty(this, "duration", PlayerUtils.durationToString(duration_))
  val playingNow = new BooleanProperty(this, "playingNow")

  private var markedDeleted_ = false
  def markDeleted { markedDeleted_ = true }
  def isMarkedDeleted = markedDeleted_

  private def cutExt(s:String) = s.reverse.dropWhile(_ != '.').drop(1).reverse.toString
}
