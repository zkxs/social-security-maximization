package net.michaelripley.socialsecurity.plan

import java.time.{LocalDate, Period, YearMonth}

import net.michaelripley.socialsecurity.Constants._
import net.michaelripley.socialsecurity.Person
import net.michaelripley.socialsecurity.SocialSecurityMath.{calculateAdjustedPayout, calculatePayout}

/**
 * A possible retirement plan for a single person
 *
 * @param person The person to calculate based on
 * @param month  Months before full retirement age
 */
case class PersonPlan(
  person: Person,
  month: Int,
) extends Plan {
  private def startOfRetirement: LocalDate = person.normalRetirementDate.plusMonths(month)

  private def monthsRedeemed: Int = Period.between(startOfRetirement, person.deathDate).toTotalMonths.toInt

  override val benefitPayouts: Map[YearMonth, BigDecimal] = {

    val benefit = person.benefitAtRetirementMonth(month)
    val start = YearMonth.from(startOfRetirement)
    val end = YearMonth.from(person.deathDate)

    LazyList.iterate(start)(_.plusMonths(1))
      .takeWhile(!_.isAfter(end))
      .map((_, benefit))
      .toMap
      .withDefaultValue(ZERO)
  }

  override def toString: String = {
    val benefit = person.primaryInsuranceAmount
    val percent = person.benefitPercentageAtRetirementMonth(month) * ONE_HUNDRED_PERCENT
    val payout = calculatePayout(benefitPayouts)
    val adjustedPayout = calculateAdjustedPayout(benefitPayouts)
    f"$startOfRetirement | $month%10d | $monthsRedeemed%3d * $benefit%5.0f * $percent%3.0f%% = $payout%5.0f | $adjustedPayout%16.0f"
  }
}
