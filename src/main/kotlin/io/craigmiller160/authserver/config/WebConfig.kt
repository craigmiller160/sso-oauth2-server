package io.craigmiller160.authserver.config

import io.craigmiller160.webutils.controller.RequestLogger
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig (
        private val requestLogger: RequestLogger
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(requestLogger)
                .addPathPatterns("/**/**")
    }

}
