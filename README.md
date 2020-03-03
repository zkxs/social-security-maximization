# Social Security Maximization
For one person this is pretty easy.

For a married couple it becomes absurdly complicated.

## Usage
```shell script
java -jar social-security-maximization-assembly-0.0.1-SNAPSHOT.jar example.toml
```

## Configuration
- An example configuration is [provided](example.toml).
- If you do not know the exact `eligibilityDate` for a person, but are certain they are eligible, simply omit it.
- Everything in the `[output]` section (including the heading) is optional, and defaults to `false` if omitted.
- The numeric values are all passed in as strings to avoid floating-point precision issues.

## Definitions
- **Retirement**: The year and month you declare your retirement for social security purposes.
  You could have stopped working and *effectively* retired years previous to this (and in fact
  it makes the math easier if you have done so).
- **Primary**: The person with the higher PIA (Primary Insurance Amount, e.g. 100% payout)
- **Secondary**: The person with the lower PIA. Note that the actual payout of the secondary may
  exceed that of the primary, for example in cases where they make similar amounts, the primary
  files early, and the secondary files late.

## Assumptions
- Neither the primary nor the secondary are working up to their retirement dates
    - There are special considerations if that is the case, as there are penalties
      for making too much money too close to retirement
    - Continuing to work in order to increase your lifetime earnings and therefore
      increase your social security payout is viable, but not supported by this program.
    - Things start to get complicated if you've worked in the three years prior to your retirement
- The primary and secondary live together
    - If this is not true, there is a $255 difference in total payout [[link]](https://www.ssa.gov/planners/survivors/ifyou.html)
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

## Current Features
- Support for single persons
- Support for married persons
    - Individual benefits
    - Spousal benefits
    - Survivor benefits
- Guaranteed to maximize total benefit for your input parameters, down to the exact month you should retire
    - Brute forces all possible retirement dates for each person and calculates total benefit for each
- Supports people born as early as 1917
- Supports inflation and interest rates

## Planned Features
*This section intentionally left blank*

## Unplanned Features
- Working until retirement
    - Effect of future income on benefit payout [[link]](https://www.ssa.gov/OACT/COLA/piaformula.html)
    - Penalty calculations \[link TBD\]

## Notes
- You will need to guess reasonable future interest and inflation rates.
- It's a good idea to assume you'll live an above-average lifespan.
