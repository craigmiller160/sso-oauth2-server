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

import io.craigmiller160.authserver.service.OAuth2ClientUserDetailsService
import io.craigmiller160.webutils.security.AuthEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class WebSecurityConfig(
    private val OAuth2ClientUserDetailsService: OAuth2ClientUserDetailsService,
    private val authEntryPoint: AuthEntryPoint
) : WebSecurityConfigurerAdapter() {

  override fun configure(http: HttpSecurity?) {
    http?.let {
      http
          .csrf()
          .disable()
          .sessionManagement()
          .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
          .and()
          .requestMatchers()
          .antMatchers("/oauth/**", "/jwk", "/ui/**", "/actuator/**")
          .and()
          .authorizeRequests()
          .antMatchers("/jwk", "/ui/**", "/oauth/auth", "/actuator/health")
          .permitAll()
          .anyRequest()
          .fullyAuthenticated()
          .and()
          .requiresChannel()
          .anyRequest()
          .requiresSecure()
          .and()
          .httpBasic()
          .and()
          .exceptionHandling()
          .authenticationEntryPoint(authEntryPoint)
    }
  }

  override fun configure(auth: AuthenticationManagerBuilder?) {
    auth?.let {
      auth.userDetailsService(OAuth2ClientUserDetailsService).passwordEncoder(passwordEncoder())
    }
  }

  @Bean
  fun passwordEncoder(): PasswordEncoder {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder()
  }
}
