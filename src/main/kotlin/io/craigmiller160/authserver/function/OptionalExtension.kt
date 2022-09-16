package io.craigmiller160.authserver.function

import arrow.core.Either
import java.util.Optional

fun <A, B> Optional<B>.rightIfNotEmpty(default: () -> A): Either<A, B> =
  when {
    isEmpty -> Either.Left(default())
    else -> Either.Right(get())
  }
