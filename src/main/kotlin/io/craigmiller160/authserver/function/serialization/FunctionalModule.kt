package io.craigmiller160.authserver.function.serialization

import arrow.core.Either
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule

fun ObjectMapper.registerFunctionalModule(): ObjectMapper {
  val module = SimpleModule()
  module.addSerializer(Either::class.java, EitherSerializer())
  return registerModule(module)
}
