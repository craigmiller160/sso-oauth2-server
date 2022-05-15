package io.craigmiller160.authserver.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.authserver.function.serialization.registerFunctionalModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
class JacksonConfig {
  // TODO remove arrow lib if doesn't work
  @Bean
  fun objectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper =
    builder.build<ObjectMapper>().registerFunctionalModule()
}
