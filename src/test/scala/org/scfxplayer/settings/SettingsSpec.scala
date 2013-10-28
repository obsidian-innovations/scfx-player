package org.scfxplayer.settings

import org.specs2.mutable._
import play.api.libs.json.{Json, Reads, Writes}
import org.scfxplayer.settings.Settings
import org.scfxplayer.utils.{JvmFileHandling, FileHandling}

class SettingsSpec extends Specification{
  "Settings" should {
    val settings = Settings("aaa")
    "encode and decode settings as Json " in {
      import Writes._
      import Reads._
      val plStr = Json.stringify(Json.toJson(settings))
      Json.parse(plStr).asOpt[Settings] must beSome(settings)
    }


    "save and open from file" in {
      implicit val fh = new FileHandling {
        def homeFolder: String =  System.getProperty("java.io.tmpdir")

        def fileSep: String = JvmFileHandling.fileSep
      }

      Settings.save(settings) must beSuccessfulTry
      Settings.open must beSuccessfulTry.withValue(settings)
    }
  }
}
