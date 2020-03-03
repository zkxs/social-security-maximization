package net.michaelripley.socialsecurity.plan

import java.time.{LocalDate, YearMonth}

import net.michaelripley.socialsecurity.Constants._
import net.michaelripley.socialsecurity.Person
import net.michaelripley.socialsecurity.SocialSecurityMath.{calculateAdjustedPayout, calculatePayout}
import net.michaelripley.socialsecurity.plan.CouplePlan._
import net.michaelripley.socialsecurity.util.Util._

object CouplePlan {
  private def startOfRetirement(person: Person, month: Int): LocalDate = person.normalRetirementDate.plusMonths(month)
}

/**
 * A plan for a couple built from the combination of their individual plans
 *
 * @param primary   the primary (higher primaryInsuranceAmount) person's plan
 * @param secondary the secondary person's plan
 */
case class CouplePlan(
  primary: Person,
  secondary: Person,
  primaryMonth: Int,
  secondaryMonth: Int,
) extends Plan {

  def primaryStartOfRetirement: LocalDate = startOfRetirement(primary, primaryMonth)

  def secondaryStartOfRetirement: LocalDate = startOfRetirement(secondary, secondaryMonth)

  override val benefitPayouts: Map[YearMonth, BigDecimal] = {
    val primaryStart = YearMonth.from(primaryStartOfRetirement)
    val secondaryStart = YearMonth.from(secondaryStartOfRetirement)
    val secondaryWidowStart = YearMonth.from(startOfRetirement(secondary, secondaryMonth).minusYears(2))
    val primaryEnd = YearMonth.from(primary.deathDate)
    val secondaryEnd = YearMonth.from(secondary.deathDate)
    val start = min(primaryStart, secondaryWidowStart)
    val end = max(primaryEnd, secondaryEnd)


    /* Once you and your spouse start receiving Social Security benefits,
     * upon the death of your spouse, you will continue to receive your benefit,
     * or your spouse’s, but not both. In addition, a surviving spouse living in
     * the same household is eligible to receive a one-time lump-sum payment of $255.
     *
     * If you have a work history, you’ll receive either your benefit or the spousal
     * benefit, whichever is greater.
     *
     * There is no benefit to delaying your spousal benefit claim past your full retirement age.
     *
     * Spousal benefits can only be claimed if the primary has already filed
     *
     * If you already receive benefits as a spouse, your benefit will automatically convert
     * to survivors benefits after we receive the report of death.
     *
     * If you are also eligible for retirement benefits (but haven't applied yet), you have an
     * additional option. You can apply for retirement or survivors benefits now and switch to
     * the other (higher) benefit at a later date.
     */

    LazyList.iterate(start)(_.plusMonths(1))
      .takeWhile(!_.isAfter(end))
      .map { yearMonth =>

        val primaryAlive = !yearMonth.isAfter(primaryEnd)
        val secondaryAlive = !yearMonth.isAfter(secondaryEnd)

        val primaryRetired = !yearMonth.isBefore(primaryStart)
        val secondaryRetired = !yearMonth.isBefore(secondaryStart)
        val secondaryWidowRetired = !yearMonth.isBefore(secondaryWidowStart) && secondaryAlive && !primaryAlive

        val primaryPayout: BigDecimal = if (primaryAlive && primaryRetired) {
          primary.benefitAtRetirementMonth(primaryMonth) // calculate primary benefit
        } else {
          ZERO
        }

        val secondaryPayout: BigDecimal = if (secondaryWidowRetired) {
          // secondary both alive and retired. Primary is deceased.
          primary.survivorBenefitAtRetirementMonth(secondaryMonth) // calculate survivor benefit
        } else if (secondaryAlive && secondaryRetired) {
          val secondaryBenefit = secondary.benefitAtRetirementMonth(secondaryMonth)
          if (primaryRetired) {
            // both primary and secondary are retired
            // primary is alive, because we have already accounted for the survivor case.
            // therefore, both secondary and spousal benefits are available to choose from
            val spousalBenefit = primary.spousalBenefitAtRetirementMonth(secondaryMonth)
            max(secondaryBenefit, spousalBenefit)
          } else {
            // primary hasn't retired, but secondary has
            // primary is alive, because we have already accounted for the survivor case.
            // therefore, only the secondary benefit is available
            secondaryBenefit
          }
        } else {
          ZERO
        }

        val lumpSumPayout: BigDecimal = if (secondaryAlive && primaryRetired && yearMonth == primaryEnd) {
          SPOUSE_DEATH_LUMP_SUM
        } else {
          ZERO
        }

        val totalBenefit = primaryPayout + secondaryPayout + lumpSumPayout
        (yearMonth, totalBenefit)
      }
      .toMap
      .withDefaultValue(ZERO)
  }

  override def toString: String = {
    val primaryDate = primaryStartOfRetirement
    val secondaryDate = secondaryStartOfRetirement
    val payout = calculatePayout(benefitPayouts)
    val adjustedPayout = calculateAdjustedPayout(benefitPayouts)
    f"primaryRetire=$primaryDate ($primaryMonth%3d), secondaryRetire=$secondaryDate ($secondaryMonth%3d); basicPayout=$payout%.0f; adjustedPayout=$adjustedPayout%.0f"
  }
}
