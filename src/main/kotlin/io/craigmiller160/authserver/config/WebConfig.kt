package io.craigmiller160.authserver.config

import io.craigmiller160.authserver.controller.NewControllerLogging
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig (
        private val newControllerLogging: NewControllerLogging
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(newControllerLogging)
                .addPathPatterns("/**/**")
    }

}
