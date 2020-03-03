package net.michaelripley.socialsecurity

object Constants {
  val ZERO = BigDecimal(0)
  val HALF = BigDecimal("0.5")
  val ONE = BigDecimal(1)
  val ONE_HUNDRED_PERCENT = BigDecimal(100)

  val MONTHS_PER_YEAR = 12

  val EARLY_RETIREMENT_MONTHLY_PENALTY: BigDecimal = BigDecimal(5) / BigDecimal(9) / ONE_HUNDRED_PERCENT
  val EARLY_RETIREMENT_MONTHLY_PENALTY_AFTER_36: BigDecimal = BigDecimal(5) / BigDecimal(12) / ONE_HUNDRED_PERCENT

  val SPOUSAL_EARLY_RETIREMENT_MONTHLY_PENALTY: BigDecimal = BigDecimal(25) / BigDecimal(36) / ONE_HUNDRED_PERCENT
  val SPOUSAL_EARLY_RETIREMENT_MONTHLY_PENALTY_AFTER_36: BigDecimal = BigDecimal(5) / BigDecimal(12) / ONE_HUNDRED_PERCENT

  val SPOUSE_DEATH_LUMP_SUM = BigDecimal("255")
}
