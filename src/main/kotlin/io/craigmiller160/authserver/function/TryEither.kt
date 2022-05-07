package io.craigmiller160.authserver.function

import arrow.core.Either
import arrow.core.computations.either
import io.craigmiller160.authserver.exception.NotFoundException

typealias TryEither<T> = Either<Throwable, T>

typealias TryEitherCompanion = Either.Companion

object tryEither {
  inline fun <T> eager(
    crossinline c: suspend arrow.core.computations.RestrictedEitherEffect<Throwable, *>.() -> T
  ) = either.eager(c)
}

fun <T> TryEitherCompanion.rightOrNotFound(value: T?, itemName: String = ""): TryEither<T> =
  value?.let { Either.Right(it) } ?: Either.Left(NotFoundException("$itemName Not Found"))
