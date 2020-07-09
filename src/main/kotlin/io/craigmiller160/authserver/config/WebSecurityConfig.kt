package io.craigmiller160.authserver.config

import io.craigmiller160.authserver.service.ClientUserDetailsService
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
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true
)
class WebSecurityConfig(
        private val clientUserDetailsService: ClientUserDetailsService,
        private val authEntryPoint: AuthEntryPoint
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        http?.let {
            http.csrf()
                    .disable()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .requestMatchers()
                    .antMatchers("/oauth/**/*", "/jwk", "/ui/**/*")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/jwk", "/ui/**/*", "/oauth/auth").permitAll()
                    .anyRequest().fullyAuthenticated()
                    .and()
                    .requiresChannel().anyRequest().requiresSecure()
                    .and()
                    .httpBasic()
                    .and()
                    .exceptionHandling().authenticationEntryPoint(authEntryPoint)
        }
    }

    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth?.let {
            auth.userDetailsService(clientUserDetailsService)
                    .passwordEncoder(passwordEncoder())
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

}
