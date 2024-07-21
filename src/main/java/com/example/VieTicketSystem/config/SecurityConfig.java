package com.example.VieTicketSystem.config;

import com.example.VieTicketSystem.repo.UserRepo;
import com.example.VieTicketSystem.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.regex.Pattern;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepo userRepo;
    private final ObjectPostProcessor<Object> objectPostProcessor;

    @Autowired
    public SecurityConfig(UserRepo userRepo, ObjectPostProcessor<Object> objectPostProcessor) {
        this.userRepo = userRepo;
        this.objectPostProcessor = objectPostProcessor;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(13);
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, UserDetailsService userDetailService)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailService)
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(new UserDetailsServiceImpl(userRepo));
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http, JwtRequestFilter jwtRequestFilter) throws Exception {
        http.securityMatcher("/api/v1/organizer/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().hasAuthority("ORGANIZER"))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.requiresChannel(channel ->
                channel.requestMatchers(new LocalRequestMatcher()).requiresInsecure()
                        .anyRequest().requiresSecure()
        );
        return http.build();
    }

    private static class LocalRequestMatcher implements RequestMatcher {
        private static final Pattern LOCAL_IP_PATTERN = Pattern.compile(
                "^(127\\.|192\\.168\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[0-1]))"
        );

        @Override
        public boolean matches(HttpServletRequest request) {
            String remoteAddr = request.getRemoteAddr();
            return LOCAL_IP_PATTERN.matcher(remoteAddr).find();
        }
    }
}
