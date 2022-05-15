package io.craigmiller160.authserver.function.serialization

import arrow.core.Either
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class EitherSerializer : StdSerializer<Either<*, *>>(Either::class.java) {
  override fun serialize(
    either: Either<*, *>?,
    jsonGenerator: JsonGenerator?,
    provider: SerializerProvider?
  ) {
    when (either) {
      is Either.Right<*> -> TODO("Figure out how to automatically serialize value")
      else -> TODO("Throw exception")
    }
  }
}
