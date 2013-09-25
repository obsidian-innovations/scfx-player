package org.scfxplayer

import scalafx.beans.property.StringProperty
import org.joda.time.Duration
import org.joda.time.format.PeriodFormatterBuilder

class MusicRecordItem(artist_ : Option[String],
                      album_ : Option[String],
                      track_ : Option[String],
                      duration_ : Duration,
                      fileName_ : String) {
  val artist = new StringProperty(this, "artist", artist_.getOrElse(""))
  val album = new StringProperty(this, "album", album_.getOrElse(""))
  val track = new StringProperty(this, "track", track_.getOrElse(""))
  val fileName = new StringProperty(this, "fileName", fileName_)
  val trackNameMade = new StringProperty(this, "trackNameMade", track_.getOrElse(cutExt(fileName_)))
  val duration = new StringProperty(this, "duration", durationToString(duration_))

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

}
