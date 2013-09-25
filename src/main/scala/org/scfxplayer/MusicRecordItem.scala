package org.scfxplayer

import scalafx.beans.property.StringProperty
import org.joda.time.Duration
import org.joda.time.format.PeriodFormatterBuilder

import play.api.libs.json._

class MusicRecordItem(val artist_ : Option[String],
                      val album_ : Option[String],
                      val track_ : Option[String],
                      val duration_ : Duration,
                      val fileName_ : String) {
  val artist = new StringProperty(this, "artist", artist_.getOrElse(""))
  val album = new StringProperty(this, "album", album_.getOrElse(""))
  val track = new StringProperty(this, "track", track_.getOrElse(""))
  val fileName = new StringProperty(this, "fileName", fileName_)
  val trackNameMade = new StringProperty(this, "trackNameMade", track_.getOrElse(cutExt(fileName_)))
  def cutExt(s:String) = s.reverse.dropWhile(_ != '.').drop(1).reverse.toString


  private def durationToString(d:Duration):String = {
    val formatter = new PeriodFormatterBuilder()
      .appendDays()
      .appendSuffix("d")
      .appendHours()
      .appendSuffix("h")
      .appendMinutes()
      .appendSuffix("m")
      .appendSeconds()
      .appendSuffix("s")
      .toFormatter()
    formatter.print(d.toPeriod())
  }

  val duration = new StringProperty(this, "duration", durationToString(duration_))

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
    } yield new MusicRecordItem(artist,track,album,duration,filename)

    def writes(o: MusicRecordItem): JsValue = obj(
      jsArtist -> o.artist_, jsTrack -> o.track_,
      jsAlbum -> o.album_, jsDuration -> o.duration_,
    jsFileName -> o.fileName_
    )
  }

  def save(filename:String, items:List[MusicRecordItem]):Either[String,Unit] = {
    import Writes._
    val playlist = Json.stringify(Json.toJson(items))
    val output = new java.io.PrintWriter(new java.io.File(filename))
    try {
      output.write(playlist)
    } finally {
      output.close()
    }
    Right(())
  }
}
