package helium.util

data class MutablePair<A, B>(
  var first: A,
  var second: B
){
  override fun toString(): String = "($first, $second)"
}

data class MutableTriple<A, B, C>(
  var first: A,
  var second: B,
  var third: C
){
  override fun toString(): String = "($first, $second, $third)"
}

infix fun <A, B> A.mto(that: B): MutablePair<A, B> = MutablePair(this, that)
infix fun <A, B, C> MutablePair<A, B>.mto(that: C) = MutableTriple(this.first, this.second, that)
