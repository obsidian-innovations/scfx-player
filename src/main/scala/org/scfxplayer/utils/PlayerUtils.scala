package org.scfxplayer.utils

import org.joda.time.Duration
import org.joda.time.format.PeriodFormatterBuilder

object PlayerUtils {

  def millisToString(v:Long):String = durationToString(new org.joda.time.Duration(v))

  def durationToString(d:Duration):String = {
    val p = d.toPeriod()
    if(p.getHours > 0)
      "%02d:%02d:%02d".format(p.getHours, p.getMinutes, p.getSeconds)
    else
      "%02d:%02d".format(p.getMinutes, p.getSeconds)
  }
}
