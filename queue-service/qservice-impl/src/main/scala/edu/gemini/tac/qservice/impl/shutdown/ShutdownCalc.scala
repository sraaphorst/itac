package edu.gemini.tac.qservice.impl.shutdown

import edu.gemini.qengine.skycalc.{RaDecBinCalc, RaBinSize}
import edu.gemini.tac.qengine.api.config.Shutdown
import edu.gemini.tac.qengine.ctx.{Site, Context}
import edu.gemini.tac.qengine.util.Time

import scala.collection.JavaConverters._

/**
 *
 */
object ShutdownCalc {

  def timePerRa(s: Shutdown, size: RaBinSize): List[Time] =
    RaDecBinCalc.RA_CALC.calc(s.site, s.start, s.end, size).asScala.toList map {
      hrs => Time.hours(hrs.getHours)
    }

  def asTime(s: Shutdown, size: RaBinSize): Time =
    Time.hours(timePerRa(s, size).map(_.toHours.value).sum)

  def trim(s: Shutdown, ctx: Context): Option[Shutdown] = {
    val semester = ctx.getSemester
    val semStart = semester.getStartDate(ctx.getSite)
    val semEnd   = semester.getEndDate(ctx.getSite)

    if ((ctx.getSite != s.site) || (s.end.compareTo(semStart) <= 0) || (s.start.compareTo(semEnd) >= 0)) None
    else {
      val newStart = if (s.start.compareTo(semStart) < 0) semStart else s.start
      val newEnd   = if (semEnd.compareTo(s.end) < 0) semEnd else s.end

      if ((s.start == newStart) && (s.end == newEnd)) Some(s)
      else Some(Shutdown(s.site, newStart, newEnd))
    }
  }

  def trim(sds: List[Shutdown], ctx: Context): List[Shutdown] =
    for {
      Some(sd) <- sds.map { trim(_, ctx) }
    } yield sd

  /**
   * Combines visible hours per RA for all the given shutdown periods into a
   * single list.
   */
  def sumHoursPerRa(shutdowns: List[Shutdown], size: RaBinSize): List[Time] = {
    val lsts  = shutdowns.map { timePerRa(_, size) }
    val zeros = List.fill(size.getBinCount)(Time.ZeroHours)

    (zeros/:lsts) {
      (sum, cur) => sum.zip(cur).map { case (t1, t2) => t1 + t2 }
    }
  }

  /**
   * Validates the collection of Shutdown, making sure that the dates for the
   * same sites don't overlap.
   */
  def validate(shutdowns: List[Shutdown]): Boolean = {
    val (north, south) = shutdowns.partition(_.site == Site.north)

    def validateDates(lst: List[Shutdown]): Boolean = {
      lst.sorted match {
        case Nil          => true
        case head :: tail =>
          val (_, res) = ((head,true)/:tail) {
            case ((last, result), cur) =>
              (cur, result && (last.end.getTime <= cur.start.getTime))
          }
          res
      }
    }

    validateDates(north) && validateDates(south)
  }
}
