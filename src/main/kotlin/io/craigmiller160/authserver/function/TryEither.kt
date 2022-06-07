package io.craigmiller160.authserver.function

import arrow.core.Either
import arrow.core.computations.either
import io.craigmiller160.authserver.dto.ExceptionConverter
import io.craigmiller160.authserver.exception.NotFoundException
import org.springframework.http.ResponseEntity

typealias TryEither<T> = Either<Throwable, T>

typealias TryEitherCompanion = Either.Companion

object tryEither {
  inline fun <T> eager(
    crossinline c: suspend arrow.core.computations.RestrictedEitherEffect<Throwable, *>.() -> T
  ) = either.eager(c)
}

fun <T> TryEitherCompanion.rightOrNotFound(value: T?, itemName: String = ""): TryEither<T> =
  value?.let { Either.Right(it) } ?: Either.Left(NotFoundException("$itemName Not Found"))

@Suppress("UNCHECKED_CAST")
fun <T> TryEither<T>.toResponseEntity(
  builder: (ResponseEntity.BodyBuilder) -> ResponseEntity.BodyBuilder = { it }
): ResponseEntity<T> =
  when (this) {
    is Either.Right<T> -> ResponseEntity.status(200).apply { builder(this) }.body(value)
    is Either.Left<Throwable> ->
      ExceptionConverter.toErrorResponseEntity(value) as ResponseEntity<T>
  }

@Suppress("UNCHECKED_CAST")
fun <T> TryEither<ResponseEntity<T>>.toResponseEntity(): ResponseEntity<T> =
  when (this) {
    is Either.Right<ResponseEntity<T>> -> value
    is Either.Left<Throwable> ->
      ExceptionConverter.toErrorResponseEntity(value) as ResponseEntity<T>
  }
