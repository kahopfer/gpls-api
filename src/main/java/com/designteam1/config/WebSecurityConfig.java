package com.designteam1.config;

import com.designteam1.security.JwtAuthenticationEntryPoint;
import com.designteam1.security.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(this.userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationTokenFilter authenticationTokenFilterBean() throws Exception {
        return new JwtAuthenticationTokenFilter();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // we don't need CSRF because our token is invulnerable
                .csrf().disable()

                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()

                // don't create session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

                .authorizeRequests()
                .antMatchers("/auth/**").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                .antMatchers(HttpMethod.POST, "/families/**").access("hasRole('ROLE_ADMIN')")
                .antMatchers(HttpMethod.PUT, "/families/**").access("hasRole('ROLE_ADMIN')")
                .antMatchers(HttpMethod.DELETE, "/families/**").access("hasRole('ROLE_ADMIN')")

                .antMatchers(HttpMethod.POST, "/students/**").access("hasRole('ROLE_ADMIN')")
                .antMatchers(HttpMethod.PUT, "/students/updateStudent/**").access("hasRole('ROLE_ADMIN')")
                .antMatchers(HttpMethod.DELETE, "/students/**").access("hasRole('ROLE_ADMIN')")

                .antMatchers(HttpMethod.POST, "/guardians/**").access("hasRole('ROLE_ADMIN')")
                .antMatchers(HttpMethod.PUT, "/guardians/**").access("hasRole('ROLE_ADMIN')")
                .antMatchers(HttpMethod.DELETE, "/guardians/**").access("hasRole('ROLE_ADMIN')")

                .antMatchers(HttpMethod.DELETE, "/lineItems/**").access("hasRole('ROLE_ADMIN')")

                .antMatchers(HttpMethod.GET, "/users/**").access("hasRole('ROLE_ADMIN')")
                .antMatchers(HttpMethod.POST, "/users/**").access("hasRole('ROLE_ADMIN')")
                .antMatchers(HttpMethod.PUT, "/users/resetPassword/**").access("hasRole('ROLE_ADMIN')")
                .antMatchers(HttpMethod.DELETE, "/users/**").access("hasRole('ROLE_ADMIN')")

                .antMatchers(HttpMethod.POST, "/priceList/**").access("hasRole('ROLE_ADMIN')")
                .antMatchers(HttpMethod.PUT, "/priceList/**").access("hasRole('ROLE_ADMIN')")
                .antMatchers(HttpMethod.DELETE, "/priceList/**").access("hasRole('ROLE_ADMIN')")

                .antMatchers(HttpMethod.GET, "/invoices/**").access("hasRole('ROLE_ADMIN')")
                .antMatchers(HttpMethod.POST, "/invoices/**").access("hasRole('ROLE_ADMIN')")
                .antMatchers(HttpMethod.PUT, "/invoices/**").access("hasRole('ROLE_ADMIN')")
                .antMatchers(HttpMethod.DELETE, "/invoices/**").access("hasRole('ROLE_ADMIN')")
                .anyRequest().authenticated();

        // Custom JWT based security filter
        httpSecurity
                .addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);

        // disable page caching
        httpSecurity.headers().cacheControl();
    }
}