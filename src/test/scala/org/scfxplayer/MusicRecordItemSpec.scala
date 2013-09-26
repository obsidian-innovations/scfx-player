package org.scfxplayer

import org.specs2.mutable._
import org.joda.time.Duration
import play.api.libs.json._

class MusicRecordItemSpec extends Specification {
  "Music Record Item" should {
    val m1 = new MusicRecordItem(Some("Artist 1"),
      Some("Album 1"),Some("Track 1"),new Duration(1000 * 60),"/home/test1")

    val m2 = new MusicRecordItem(Some("Artist 2"),
      Some("Album 2"),Some("Track 2"),new Duration(1000 * 30),"/home/test2")



    "encode and decode the same object as Json " in {
      val m1Str = Json.stringify(Json.toJson(m1))
      Json.parse(m1Str).asOpt[MusicRecordItem] must beSome(m1)
    }


  }
}
