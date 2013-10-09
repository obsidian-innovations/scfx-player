package org.scfxplayer

trait FileHandling {
  def homeFolder:String
  def fileSep:String
}

object JvmFileHandling extends FileHandling {
  def homeFolder: String = System.getProperty("user.home")

  def fileSep: String = System.getProperty("file.separator")
}