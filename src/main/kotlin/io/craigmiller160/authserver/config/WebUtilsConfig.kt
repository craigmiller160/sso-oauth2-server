package io.craigmiller160.authserver.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackages = [
    "io.craigmiller160.webutils.controller",
    "io.craigmiller160.webutils.util"
])
class WebUtilsConfig