package net.michaelripley.socialsecurity

import java.time.LocalDate

import net.michaelripley.socialsecurity.Constants.{EARLY_RETIREMENT_MONTHLY_PENALTY, EARLY_RETIREMENT_MONTHLY_PENALTY_AFTER_36, ZERO, _}
import net.michaelripley.socialsecurity.Person._
import net.michaelripley.socialsecurity.util.Util._

object Person {
  private val DELAYED_RETIREMENT_MIN = BigDecimal(".03")
  private val DELAYED_RETIREMENT_SCALE_START = BigDecimal(".035")
  private val DELAYED_RETIREMENT_SCALE_QUANTITY = BigDecimal("0.005")
  private val DELAYED_RETIREMENT_MAX = BigDecimal(".08")

  /**
   * Calculate retirement date from birth-date. This is kept separate from the Person instance
   * because in the case of the survivors the birth-date input is tampered with.
   *
   * @param birthDate The birth-date
   * @return the retirement date
   */
  private def retirementDate(birthDate: LocalDate): LocalDate = {
    birthDate.minusDays(1).getYear match {
      case y if y <= 1937 => birthDate.plusYears(65)
      case y if y == 1938 => birthDate.plusYears(65).plusMonths(2)
      case y if y == 1939 => birthDate.plusYears(65).plusMonths(4)
      case y if y == 1940 => birthDate.plusYears(65).plusMonths(6)
      case y if y == 1941 => birthDate.plusYears(65).plusMonths(8)
      case y if y == 1942 => birthDate.plusYears(65).plusMonths(10)
      case y if y >= 1943 && y <= 1954 => birthDate.plusYears(66)
      case y if y == 1955 => birthDate.plusYears(66).plusMonths(2)
      case y if y == 1956 => birthDate.plusYears(66).plusMonths(4)
      case y if y == 1957 => birthDate.plusYears(66).plusMonths(6)
      case y if y == 1958 => birthDate.plusYears(66).plusMonths(8)
      case y if y == 1959 => birthDate.plusYears(66).plusMonths(10)
      case y if y >= 1960 => birthDate.plusYears(67)
    }
  }

  /**
   * Calculate the early retirement penalty
   *
   * @param months number of months before normal retirement age
   * @return the penalty multiplier (e.g. 0.3 for a 30% penalty)
   */
  private def earlyRetirementPenalty(months: Int): BigDecimal = {
    if (months <= 0) {
      ZERO
    } else if (months > 36) {
      EARLY_RETIREMENT_MONTHLY_PENALTY * 36 + EARLY_RETIREMENT_MONTHLY_PENALTY_AFTER_36 * (months - 36)
    } else {
      EARLY_RETIREMENT_MONTHLY_PENALTY * months
    }
  }: BigDecimal

  /**
   * Calculate the spousal early retirement penalty
   *
   * @param months number of months before normal retirement age
   * @return the penalty multiplier (e.g. 0.3 for a 30% penalty)
   */
  private def spousalEarlyRetirementPenalty(months: Int): BigDecimal = {
    if (months <= 0) {
      ZERO
    } else if (months > 36) {
      SPOUSAL_EARLY_RETIREMENT_MONTHLY_PENALTY * 36 + SPOUSAL_EARLY_RETIREMENT_MONTHLY_PENALTY_AFTER_36 * (months - 36)
    } else {
      SPOUSAL_EARLY_RETIREMENT_MONTHLY_PENALTY * months
    }
  }: BigDecimal

  /**
   * Calculate the spousal percentage of the normal benefit payed out for a given retirement month
   *
   * @param month the secondary's offset in months from normal retirement age. Negative indicates early retirement, positive indicates late retirement.
   * @return the secondary's benefit percentage for the given retirement month
   */
  private def spousalBenefitPercentageAtRetirementMonth(month: Int): BigDecimal = {
    if (month == 0) {
      HALF
    } else if (month < 0) {
      HALF - spousalEarlyRetirementPenalty(-month)
    } else { // implies (month > 0)
      HALF // your delayed retirement credits do NOT carry over to your spouse
    }
  }

  /**
   * Calculate the survivor percentage of the normal benefit payed out for a given retirement month
   *
   * @param month the survivor's offset in months from their NORMAL retirement age. Negative indicates early retirement, positive indicates late retirement.
   * @return the survivor's benefit percentage for the given retirement month
   */
  private def survivorBenefitPercentageAtRetirementMonth(month: Int): BigDecimal = {
    // survivors can start two years earlier, so we need to shift two years before applying the penalty
    // the penalty at the earliest retirement age is still 70% for survivors
    // e.g. we'd adjust a -84 into a -60 for this check
    val survivorMonth = month + 24
    if (survivorMonth == 0) {
      ONE
    } else if (survivorMonth < 0) {
      ONE - earlyRetirementPenalty(-survivorMonth)
    } else { // implies (month > 0)
      ONE
    }
  }
}

/**
 * Represents a person
 *
 * @param birthDate              date of birth
 * @param eligibilityDate        date at which you have enough credits to become eligible
 * @param deathDate              estimated date of death
 * @param primaryInsuranceAmount 100% social security payout. This is what you get if you retire at your normal retirement age
 */
case class Person(
  birthDate: LocalDate,
  eligibilityDate: Option[LocalDate],
  deathDate: LocalDate,
  primaryInsuranceAmount: BigDecimal,
) {

  /**
   * The normal retirement age. This is where you get 100% of your normal benefit.
   */
  val normalRetirementDate: LocalDate = retirementDate(birthDate)

  /**
   * The survivor retirement age. This is where you get 100% of your survivor benefit.
   */
  def survivorRetirementDate: LocalDate = retirementDate(birthDate.minusYears(2))

  /**
   * The delayed retirement credit given per year over the full retirement age
   * Dividing this by 12 yields the monthly delayed retirement credits
   */
  private def yearlyDelayedRetirementCredit: BigDecimal = {
    birthDate.minusDays(1).getYear match {
      case y if y >= 1917 && y <= 1924 => DELAYED_RETIREMENT_MIN
      case y if y >= 1925 && y <= 1942 => ((y - 1925) / 2) * DELAYED_RETIREMENT_SCALE_QUANTITY + DELAYED_RETIREMENT_SCALE_START
      case y if y >= 1943 => DELAYED_RETIREMENT_MAX
      //TODO: handle people born before 1917
    }
  }

  /**
   * The delayed retirement credit given per month over the full retirement age
   */
  val monthlyDelayedRetirementCredit: BigDecimal = yearlyDelayedRetirementCredit / 12

  private def earliestRetirementIfEligible: LocalDate = birthDate.plusYears(62) // earliest retirement date, assuming you have enough credits

  /**
   * The earliest you can retire. This is when you have 40 credits or when you turn 62, whatever happens last.
   */
  def earliestRetirement: LocalDate = {
    eligibilityDate match {
      case Some(eDate) => max(earliestRetirementIfEligible, eDate) // you can't retire before you are eligible
      case None => earliestRetirementIfEligible
    }
  }

  /**
   * The latest you can retire. This is the age at which you stop gaining delayed retirement credit.
   */
  def latestRetirement: LocalDate = birthDate.plusYears(70)

  /**
   * Calculate the percentage of the normal benefit payed out for a given retirement month
   *
   * @param month offset in months from normal retirement age. Negative indicates early retirement, positive indicates late retirement.
   * @return the benefit percentage for the given retirement month
   */
  def benefitPercentageAtRetirementMonth(month: Int): BigDecimal = {
    if (month == 0) {
      ONE
    } else if (month < 0) {
      ONE - earlyRetirementPenalty(-month)
    } else { // implies (month > 0)
      ONE + monthlyDelayedRetirementCredit * month
    }
  }

  /**
   * Calculate the benefit payed out for a given retirement month
   *
   * @param month offset in months from normal retirement age. Negative indicates early retirement, positive indicates late retirement.
   * @return the benefit for the given retirement month
   */
  def benefitAtRetirementMonth(month: Int): BigDecimal = {
    benefitPercentageAtRetirementMonth(month) * primaryInsuranceAmount
  }

  /**
   * Calculate the spousal benefit payed out for a given retirement month
   *
   * @param month the spouse's offset in months from normal retirement age. Negative indicates early retirement, positive indicates late retirement.
   * @return the spouse's benefit for the given retirement month
   */
  def spousalBenefitAtRetirementMonth(month: Int): BigDecimal = {
    spousalBenefitPercentageAtRetirementMonth(month) * primaryInsuranceAmount
  }

  /**
   * Calculate the survivor benefit payed out for a given retirement month
   *
   * @param month the survivor's offset in months from their NORMAL retirement age. Negative indicates early retirement, positive indicates late retirement.
   * @return the survivor's benefit for the given retirement month
   */
  def survivorBenefitAtRetirementMonth(month: Int): BigDecimal = {
    survivorBenefitPercentageAtRetirementMonth(month) * primaryInsuranceAmount
  }

  override def toString: String = s"birth=$birthDate, normalRetirement=$normalRetirementDate"
}
