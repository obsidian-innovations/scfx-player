package org.scfxplayer.async

import akka.actor.{Actor}
import java.util.concurrent.atomic.AtomicInteger
import org.scfxplayer.PlayListManagerMenu
import org.slf4j.LoggerFactory

case object Processed

class AddFilesActor(fs:Seq[java.io.File],plMgr:PlayListManagerMenu) extends Actor {
  private val logger = LoggerFactory.getLogger(this.getClass);
  private val counter = new AtomicInteger(fs.filter(_.exists).map(_.getAbsolutePath).distinct.size)

  def receive = {
    case Processed => {
      val currCounter = counter.decrementAndGet()
      logger.info(s"$currCounter remaining files to add")
      if(currCounter == 0) plMgr.saveCurrentPlaylist()
    }
  }
}
