package net.michaelripley.socialsecurity

import java.net.URI

import net.michaelripley.socialsecurity.Constants._
import toml.Codecs._
import toml.Parse.{Address, Message}
import toml.{Codec, Toml, Value}

import scala.io.Source

object Configuration {
  private implicit val decimalCodec: Codec[BigDecimal] = Codec {
    case (Value.Str(value), _, _) =>
      try {
        Right(BigDecimal(value))
      } catch {
        case _: NumberFormatException => Left((Nil, s"""Invalid decimal: "$value". Expected a decimal in the format: "1.23""""))
      }
    case (value, _, _) => Left((Nil, s"""Invalid decimal: "$value". Expected a decimal in the format: "1.23""""))
  }

  def load(uri: URI): Either[(Address, Message), Configuration] = {
    val source = Source.fromURI(uri)
    val toml = source.getLines().mkString("\n")
    val configuration = parse(toml)
    source.close()
    configuration
  }

  private def parse(toml: String): Either[(Address, Message), Configuration] = {
    Toml.parseAs[Configuration](toml)
  }
}

case class Configuration(
  primary: Person,
  secondary: Person,
  rates: Rates,
  output: Option[Output]
)

case class Rates(
  yearlyInterestRate: BigDecimal,
  yearlyInflationRate: BigDecimal,
) {
  def combinedMonthlyRate: BigDecimal = (yearlyInterestRate - yearlyInflationRate) / MONTHS_PER_YEAR
}

case class Output(
  worst: Option[Boolean],
  monthlyBreakdown: Option[Boolean],
  primaryBreakdown: Option[Boolean],
)
