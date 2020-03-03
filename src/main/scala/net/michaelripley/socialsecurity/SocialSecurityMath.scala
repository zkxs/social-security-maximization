package net.michaelripley.socialsecurity

import java.nio.file.Paths
import java.time.temporal.ChronoUnit
import java.time.{YearMonth, ZonedDateTime}

import net.michaelripley.socialsecurity.Constants._
import net.michaelripley.socialsecurity.plan.{CouplePlan, Plan}

object SocialSecurityMath { // assume primary has a higher primaryInsuranceAmount than secondary

  /**
   * dollar amounts will be displayed using values adjusted to this day
   */
  private val presentDate = ZonedDateTime.now()

  /**
   * horrible hack to allow global access to adjusted payout functions
   */
  private var configuration: Option[Configuration] = None

  /**
   * combined growth rate (interest - inflation) per month
   */
  private def monthlyGrowthRate: BigDecimal = configuration.get.rates.combinedMonthlyRate

  def main(args: Array[String]): Unit = {
    if (args.length > 0) {
      val path = Paths.get(args(0))
      Configuration.load(path.toUri) match {
        case Left((_, message)) =>
          System.err.println(s"Could not parse configuration: $message")
        case Right(config) =>
          configuration = Some(config)
          run(config)
      }
    } else {
      System.err.println("Path to a configuration file must be passed in as the first argument.")
    }
  }

  def run(configuration: Configuration): Unit = {

    val primary = configuration.primary
    val secondary = configuration.secondary

    val worst = configuration.output.flatMap(_.worst).getOrElse(false)
    val monthlyBreakdown = configuration.output.flatMap(_.monthlyBreakdown).getOrElse(false)
    val primaryBreakdown = configuration.output.flatMap(_.primaryBreakdown).getOrElse(false)

    println("Working... (this may take some time)")

    // this will take a while...
    val singlePlans = Plan(primary)
    val couplePlans = Plan(primary, secondary)
    val worstPlan = if (worst) {
      Some(couplePlans.minBy(plan => calculateAdjustedPayout(plan.benefitPayouts)))
    } else {
      None
    }
    val normalPlan = CouplePlan(primary, secondary, 0, 0)
    val delayPlan = plan.CouplePlan(primary, secondary, 36, 0)
    val bestPlan = couplePlans.maxBy(plan => calculateAdjustedPayout(plan.benefitPayouts))

    // done with costly work, print results.
    println(f"monthly growth rate: ${monthlyGrowthRate * ONE_HUNDRED_PERCENT}%.3f%%")
    println(s"primary:   $primary")
    println(s"secondary: $secondary")
    worstPlan match {
      case Some(plan) => println(s"worst: $plan")
      case None => // do nothing
    }
    println(s"100%:  $normalPlan")
    println(s"delay: $delayPlan")
    println(s"best:  $bestPlan")

    if (monthlyBreakdown) {
      println()
      println("Monthly payouts of best plan:")
      println(" Month  |  Flat | Adjusted")
      println("------- + ----- + --------")
      printPayouts(bestPlan.benefitPayouts)
    }

    if (primaryBreakdown) {
      println()
      println("All possible single person plans for the primary:")
      println("Retirement | NRA offset |     benefit calculation     | adjusted benefit")
      println("---------- + ---------- + --------------------------- + ----------------")
      singlePlans.foreach(println)
    }
  }

  def calculatePayout(payouts: Map[YearMonth, scala.BigDecimal]): BigDecimal = {
    payouts.values.sum
  }

  def printPayouts(payouts: Map[YearMonth, scala.BigDecimal]): Unit = {
    payouts
      .toSeq
      .sortBy(_._1)
      .foreach(
        { tuple =>
          val (yearMonth, benefit) = tuple
          val adjustedBenefit = presentValue(yearMonth, benefit)
          if (benefit > 0.01) {
            println(f"$yearMonth | $benefit%5.0f | $adjustedBenefit%8.0f")
          }
        }
      )
  }

  def calculateAdjustedPayout(payouts: Map[YearMonth, scala.BigDecimal]): BigDecimal = {
    payouts
      .map(
        { tuple =>
          val (yearMonth, benefit) = tuple
          presentValue(yearMonth, benefit)
        }
      )
      .sum
  }

  private def presentValue(futureDate: YearMonth, futureValue: BigDecimal): BigDecimal = {
    val periods: Int = YearMonth.from(presentDate).until(futureDate, ChronoUnit.MONTHS).toInt
    futureValue / (ONE + monthlyGrowthRate).pow(periods)
  }

}
