package net.michaelripley.socialsecurity

import scala.io.Source

object Tables {
  /**
   * Map from year to average wage index for that year.
   */
  lazy val averageWageIndex: Map[Int, BigDecimal] = {
    // We always index an individual's earnings to the average wage level two years prior to the year of first eligibility.
    val source = Source.fromResource("national-average-wage-index.csv")
    val awi = source.getLines()
      .map(_.split("\t"))
      .map { case Array(year, awi) =>
        (year.toInt, BigDecimal(awi))
      }
      .toMap
    source.close()
    awi
  }

  def main(args: Array[String]): Unit = {
    println(s"api(2018) = ${averageWageIndex(2018)}")
  }
}
