package io.craigmiller160.authserver.config

import io.craigmiller160.authserver.controller.NewControllerLogging
import io.craigmiller160.oauth.utils.controller.RequestLogger
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@Import(RequestLogger::class)
class WebConfig (
        private val requestLogger: RequestLogger
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        println("AddInterceptor: $requestLogger") // TODO delete this
        registry.addInterceptor(requestLogger)
                .addPathPatterns("/**/**")
    }

}
