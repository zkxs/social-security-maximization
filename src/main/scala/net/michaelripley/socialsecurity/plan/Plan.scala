package net.michaelripley.socialsecurity.plan

import java.time.{Period, YearMonth}

import net.michaelripley.socialsecurity.Person
import net.michaelripley.socialsecurity.util.Util._

object Plan {
  /**
   * Compute all possible plans for a person
   *
   * @param person the person
   * @return all possible plans
   */
  def apply(person: Person): Iterable[PersonPlan] = {
    monthRange(person).map(month => PersonPlan(person, month))
  }

  /**
   * Compute all possible plans for a couple
   *
   * @param primary   the primary
   * @param secondary the secondary
   * @return all possible plans
   */
  def apply(primary: Person, secondary: Person): Iterable[CouplePlan] = {
    val primaryRange = monthRange(primary)
    val secondaryRange = monthRange(secondary)
    primaryRange.crossProduct(secondaryRange)
      .map { tuple =>
        val (primaryMonth, secondaryMonth) = tuple
        CouplePlan(primary, secondary, primaryMonth, secondaryMonth)
      }
  }

  private def monthRange(person: Person) = {
    val minMonths: Int = -Period.between(person.earliestRetirement, person.normalRetirementDate).toTotalMonths.toInt
    val maxMonths: Int = Period.between(person.normalRetirementDate, person.latestRetirement).toTotalMonths.toInt
    minMonths to maxMonths
  }
}

trait Plan {
  val benefitPayouts: Map[YearMonth, BigDecimal]
}
