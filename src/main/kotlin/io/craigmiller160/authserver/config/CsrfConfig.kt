/*
 *     sso-oauth2-server
 *     Copyright (C) 2020 Craig Miller
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.craigmiller160.authserver.config

import org.apache.catalina.filters.RestCsrfPreventionFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

//@Configuration
class CsrfConfig {

//    @Bean
    fun restCsrfPreventionFilter(): FilterRegistrationBean<RestCsrfPreventionFilter> {
        val filter = RestCsrfPreventionFilter()
        filter.denyStatus = 403
        filter.setPathsAcceptingParams("/oauth/auth")
        val filterRegistration = FilterRegistrationBean(filter)
        filterRegistration.order = Integer.MIN_VALUE
        filterRegistration.urlPatterns = listOf("/oauth/auth", "/ui/csrf")
        return filterRegistration
    }

}