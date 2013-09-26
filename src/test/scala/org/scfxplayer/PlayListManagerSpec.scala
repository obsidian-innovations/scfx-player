package org.scfxplayer

import org.specs2.mutable._
import org.joda.time.Duration
import play.api.libs.json.{Json, Reads, Writes}

class PlayListManagerSpec extends Specification {
  val m1 = new MusicRecordItem(Some("Artist 1"),
    Some("Album 1"),Some("Track 1"),new Duration(1000 * 60),"/home/test1")

  val m2 = new MusicRecordItem(Some("Artist 2"),
    Some("Album 2"),Some("Track 2"),new Duration(1000 * 30),"/home/test2")
  val playlist = List(m1,m2)


  "Playlist manager" should {
    "encode and decode a playlist as Json " in {
      import Writes._
      import Reads._
      val plStr = Json.stringify(Json.toJson(playlist))
      Json.parse(plStr).asOpt[List[MusicRecordItem]] must beSome(playlist)
    }

    "save and load from file" in {
      val file = java.io.File.createTempFile("tmp-play","json")
      PlayListManager.save(file.getAbsolutePath,playlist) must beSuccessfulTry
      PlayListManager.open(file.getAbsolutePath) must beSuccessfulTry.withValue(playlist)
    }

    "get default location for playlists" in {
      val fh = new FileHandling {
        def homeFolder: String = "/test"

        def fileSep: String = "/"
      }

      PlayListManager.defaultLocation(fh) must
        beSuccessfulTry.withValue(s"/test/${PlayListManager.playerHomeName}/${PlayListManager.defaultPlaylistName}")
    }


    "save and open default" in {
      val fh = new FileHandling {
        def homeFolder: String =  System.getProperty("java.io.tmpdir")

        def fileSep: String = JvmFileHandling.fileSep
      }

      PlayListManager.saveToDefault(playlist) must beSuccessfulTry
      PlayListManager.openDefault() must beSuccessfulTry.withValue(playlist)
    }
  }
}
