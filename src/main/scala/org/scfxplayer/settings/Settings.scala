package org.scfxplayer.settings

import sorm.{InitMode, Entity, Instance}
import scala.util.Try
import org.scfxplayer.utils.JvmFileHandling

object ConfigKey extends Enumeration {
  type ConfigKey = Value
  val PlaylistLocation = Value
}

object Settings {

  case class ConfigPair(key: ConfigKey.Value, value: String)

  lazy val DbSettings = new Instance (
    entities = Set(Entity[ConfigPair](unique = Set() + Seq("key"))),
    url = "jdbc:h2:file:~/.scfx-player/settings",
    user = "",
    password = "",
    initMode = InitMode.Create,
    poolSize = 3
  )

  def playlistLocation: Try[String] = Try {
    DbSettings
      .query[ConfigPair]
      .whereEqual("key", ConfigKey.PlaylistLocation)
      .fetchOne()
      .map(pair => pair.value)
      .orElse(Some(s"${JvmFileHandling.homeFolder}/.scfx-player/scfx-def-playlist.playlist"))
      .get
  }

  def savePlaylistLocation(path: String): Try[String] = Try {
    DbSettings
      .query[ConfigPair]
      .whereEqual("key", ConfigKey.PlaylistLocation)
      .fetchOne()
      .orElse(Some(ConfigPair(ConfigKey.PlaylistLocation, path)))
      .map(pair => pair.copy(value = path))
      .map(DbSettings.save)
      .map(pair => pair.value)
      .get
  }

}

