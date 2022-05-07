package io.craigmiller160.authserver.function

class ReturnUnion2<A, B> private constructor(private val a: A? = null, private val b: B? = null) {
  companion object {
    fun <A, B> ofA(a: A): ReturnUnion2<A, B> = ReturnUnion2(a, null)
    fun <A, B> ofB(b: B): ReturnUnion2<A, B> = ReturnUnion2(null, b)
  }

  init {
    if ((a == null && b == null) || (a != null && b != null)) {
      throw IllegalStateException("ReturnUnion2 must have exactly one non-null argument")
    }
  }

  fun <R> fold(mapA: (A) -> R, mapB: (B) -> R): R {
    if (a != null) {
      return mapA(a)
    }
    return mapB(b!!)
  }
}