# Social Security Maximization
For one person this is pretty easy.

For a married couple it becomes absurdly complicated.

### Table of Contents
- [Definitions](#definitions)
- [Setup](#setup)
- [Usage](#usage)
- [Configuration](#configuration)
- [Assumptions](#assumptions)
- [Features](#features)
    - [Current Features](#current-features)
    - [Planned Features](#planned-features)
    - [Unplanned Features](#unplanned-features)

## Definitions
- **Retirement**: The year and month you declare your retirement for social security purposes.
  You could have stopped working and *effectively* retired years previous to this (and in fact
  it makes the math easier if you have done so).
- **Primary Insurance Amount (PIA)**: The benefit (before rounding down to next lower whole dollar) a person would receive if he/she elects to begin receiving retirement benefits at his/her normal retirement age. <sup>[[link][pia]]</sup>
- **Normal Retirement Age (NRA)**: The age at which retirement benefits (before rounding) are equal to the primary insurance amount. <sup>[[link][nra]]</sup>
- **Primary**: The person with the higher PIA.
- **Secondary**: The person with the lower PIA. Note that the actual payout of the secondary may
  exceed that of the primary, for example in cases where they make similar amounts, the primary
  files early, and the secondary files late.

## Setup
1. Ensure you have [Java](https://java.com/en/download/) installed.
1. Go to the [latest release page](https://github.com/zkxs/social-security-maximization/releases/latest).
2. Download the jar file to a location of your choosing.
3. Make a configuration file. See the [configuration section](#configuration) below for documentation.

## Usage
```shell script
java -jar social-security-maximization-assembly-0.0.1.jar example.toml
```

## Configuration
| Configuration                    | Description                                                                    | Example      |
| -------------------------------- | ------------------------------------------------------------------------------ | ------------ |
| `primary.birthDate`              | Date the primary was born.                                                     | `1945-02-15` |
| `primary.eligibilityDate`        | Date the primary becomes eligible for social security benefits. (40 credits)   | `1980-02-15` |
| `primary.deathDate`              | Estimated death date of the primary.                                           | `2032-02-15` |
| `primary.primaryInsuranceAmount` | The PIA of the primary, in dollars.                                            | `"2400"`     |
| `secondary.*`                    | Same as primary for all configurations.                                        |              |
| `rates.yearlyInterestRate`       | Interest rate compounded yearly. Example shows 5%.                             | `"0.05"`     |
| `rates.yearlyInflationRate`      | Inflation rate compounded yearly. Example shows 2.5%.                          | `"0.025"`    |
| `output.worst`                   | Show the worst possible plan. Not useful aside from comparison to other plans. | `false`      |
| `output.monthlyBreakdown`        | Show a monthly payout breakdown with both flat and growth-adjusted values.     | `false`      |
| `output.primaryBreakdown`        | Show a breakdown for all of the primary's possible plans.                      | `false`      |

- An example configuration is [provided here](example.toml).
- If you do not know the exact `eligibilityDate` for a person, but are certain they are eligible, simply omit it.
- Everything in the `[output]` section (including the heading) is optional, and defaults to `false` if omitted.
- You will need to guess reasonable future interest and inflation rates.
- It's a good idea to assume you'll live an above-average lifespan.
- The numeric values are all passed in as strings to avoid floating-point precision issues.

## Assumptions
If any of these assumptions are false, the output is not guaranteed to be correct.

- Neither the primary nor the secondary are working up to their retirement dates
    - There are special considerations if that is the case, as there are penalties
      for making too much money too close to retirement
    - Continuing to work in order to increase your lifetime earnings and therefore
      increase your social security payout is viable, but not supported by this program.
    - Things start to get complicated if you've worked in the three years prior to your retirement
- The primary and secondary live together
    - If this is not true, there is a $255 difference in total payout <sup>[[link](https://www.ssa.gov/planners/survivors/ifyou.html)]</sup>
- No divorce/remarry shenanigans have occurred
    - If you have multiple eligible ex-spouses you can take your pick
    - Implementation-wise, this math would be remarkably similar to polygamy math
    - Duration of marriage becomes important (10 year minimum)
- The primary has significantly higher PIA than the secondary
    - There are interesting edge cases when the secondary makes nearly as much as the primary
      and I'm not sure I've fully grokked them
        - one spouse continues working to increase PIA?
        - could cause secondary PIA to pass primary PIA?
        - potential survivor benefit switcharoos?
- The spouse is the only one claiming benefits derived from the primary
    - There are limits that apply to how much payout can be distributed to family members of the primary.
      These limits only kick in if there are several family members claiming benefits from the same primary.

## Features

### Current Features
- Support for single persons
- Support for married persons
    - Individual benefits
    - Spousal benefits
    - Survivor benefits
- Guaranteed to maximize total benefit for your input parameters, down to the exact month you should retire
    - Brute forces all possible retirement dates for each person and calculates total benefit for each
- Supports people born as early as 1917
- Supports inflation and interest rates

### Planned Features
*This section intentionally left blank*

### Unplanned Features
- Working until retirement
    - Effect of future income on benefit payout <sup>[[link][pia]]</sup>
    - Penalty calculations <sup>\[link TBD\]</sup>

[nra]: https://www.ssa.gov/OACT/ProgData/nra.html
[pia]: https://www.ssa.gov/OACT/COLA/piaformula.html
