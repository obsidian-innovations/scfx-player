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

object MusicRecordItem {

  val jsArtist = "artist"
  val jsTrack = "track"
  val jsAlbum = "album"
  val jsDuration = "duration"
  val jsFileName = "filename"

  implicit val durationFormat = new Format[Duration] {
    def reads(json: JsValue): JsResult[Duration] = json.validate[Long].map(l => new Duration(l))

    def writes(o: Duration): JsValue = Json.toJson(o.getMillis)
  }


  implicit val format = new Format[MusicRecordItem] {
    import Json._

    def reads(json: JsValue): JsResult[MusicRecordItem] = for {
      artist <- (json \ jsArtist).validate[Option[String]]
      track <- (json \ jsTrack).validate[Option[String]]
      album <- (json \ jsAlbum).validate[Option[String]]
      duration <- (json \ jsDuration).validate[Duration]
      filename <- (json \ jsFileName).validate[String]
    } yield new MusicRecordItem(artist,album,track,duration,filename)

    def writes(o: MusicRecordItem): JsValue = obj(
      jsArtist -> o.artist_, jsTrack -> o.track_,
      jsAlbum -> o.album_, jsDuration -> o.duration_,
    jsFileName -> o.fileName_
    )
  }

  def save(filename:String, items:List[MusicRecordItem]):Try[Unit] = Try {
    import Writes._
    val playlist = Json.stringify(Json.toJson(items))
    val output = new java.io.PrintWriter(new java.io.File(filename))
    try {
      output.write(playlist)
    } finally {
      output.close()
    }
  }

  def open(filename:String):Try[List[MusicRecordItem]] = Try {
    val lines = scala.io.Source.fromFile(filename).mkString
    Json.parse(lines).as[List[MusicRecordItem]]
  }
}
