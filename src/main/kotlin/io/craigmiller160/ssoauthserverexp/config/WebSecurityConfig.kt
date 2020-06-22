package io.craigmiller160.ssoauthserverexp.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy

@Configuration
@EnableWebSecurity
class WebSecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        http?.let {
            http.csrf()
                    .disable()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .requestMatchers()
                    .antMatchers("/oauth/**/*")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/oauth/token").permitAll()
                    .anyRequest().fullyAuthenticated()
                    .and()
                    .requiresChannel().anyRequest().requiresSecure()
        }
    }

}