package io.craigmiller160.authserver.function

import arrow.core.Either

typealias TryEither<T> = Either<Throwable, T>
