package net.michaelripley.socialsecurity.util

object Util {
  def min[T <: Comparable[_]](dates: T*): T = dates.min
  def max[T <: Comparable[_]](dates: T*): T = dates.max
  def min(numbers: BigDecimal*): BigDecimal = numbers.min
  def max(numbers: BigDecimal*): BigDecimal = numbers.max

  implicit class Crossable[T](x: Iterable[T]) {
    def crossProduct(y: Iterable[T]): Iterable[(T, T)] = for (i <- x; j <- y) yield (i, j)
  }
}
