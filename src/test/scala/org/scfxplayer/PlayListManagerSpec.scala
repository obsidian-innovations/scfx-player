package org.scfxplayer

import org.specs2.mutable._
import org.joda.time.Duration
import play.api.libs.json.{Json, Reads, Writes}

class PlayListManagerSpec extends Specification {
  val m1 ="/home/test1"

  val m2 = "/home/test2"
  val playlist = PlayList(List(m1,m2))


  "Playlist manager" should {
    "encode and decode a playlist as Json " in {
      import Writes._
      import Reads._
      val plStr = Json.stringify(Json.toJson(playlist))
      Json.parse(plStr).asOpt[PlayList] must beSome(playlist)
    }

    "save and load from file" in {
      val file = java.io.File.createTempFile("tmp-play","json")
      PlayListManager.save(file.getAbsolutePath,playlist) must beSuccessfulTry
      PlayListManager.open(file.getAbsolutePath) must beSuccessfulTry.withValue(playlist)
    }

//    "get default location for playlists" in {
//      val fh = new FileHandling {
//        def homeFolder: String = "/test"
//        def fileSep: String = "/"
//      }
//
//      PlayListManager.defaultLocation(fh) must
//        beSuccessfulTry.withValue(s"${System.getProperty("user.dir")}/${PlayListManager.playerHomeName}/${PlayListManager.defaultPlaylistName}")
//    }

    "save and open default" in {
      implicit val fh = new FileHandling {
        def homeFolder: String =  System.getProperty("java.io.tmpdir")

        def fileSep: String = JvmFileHandling.fileSep
      }

      PlayListManager.saveToDefault(playlist) must beSuccessfulTry
      PlayListManager.openDefault must beSuccessfulTry.withValue(playlist)
    }
  }
}
