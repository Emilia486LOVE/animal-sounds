package com.example.animalvoiceprint.config;

import com.example.animalvoiceprint.security.CustomUserDetailsService;
import com.example.animalvoiceprint.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, CustomUserDetailsService customUserDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/captcha").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/statistics/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/datasets/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/audio/download/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/audio/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/labels/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/train/tasks").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/train/tasks/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/evaluation/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/prediction/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/annotations/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/datasets/**").hasAnyRole("admin", "algorithm")
                .requestMatchers(HttpMethod.PUT, "/api/datasets/**").hasAnyRole("admin", "algorithm")
                .requestMatchers(HttpMethod.DELETE, "/api/datasets/**").hasAnyRole("admin", "algorithm")
                .requestMatchers(HttpMethod.POST, "/api/audio/**").hasAnyRole("admin", "annotator", "algorithm")
                .requestMatchers(HttpMethod.PUT, "/api/audio/**").hasAnyRole("admin", "annotator", "algorithm")
                .requestMatchers(HttpMethod.DELETE, "/api/audio/**").hasAnyRole("admin", "annotator", "algorithm")
                .requestMatchers(HttpMethod.POST, "/api/labels/**").hasAnyRole("admin", "algorithm")
                .requestMatchers(HttpMethod.PUT, "/api/labels/**").hasAnyRole("admin", "algorithm")
                .requestMatchers(HttpMethod.DELETE, "/api/labels/**").hasAnyRole("admin", "algorithm")
                .requestMatchers(HttpMethod.POST, "/api/annotations/**").hasAnyRole("admin", "annotator")
                .requestMatchers(HttpMethod.PUT, "/api/annotations/**").hasAnyRole("admin", "annotator")
                .requestMatchers(HttpMethod.DELETE, "/api/annotations/**").hasAnyRole("admin", "annotator")
                .requestMatchers(HttpMethod.POST, "/api/train/tasks").hasAnyRole("admin", "algorithm")
                .requestMatchers(HttpMethod.PUT, "/api/train/tasks/**").hasAnyRole("admin", "algorithm")
                .requestMatchers(HttpMethod.DELETE, "/api/train/tasks/**").hasAnyRole("admin", "algorithm")
                .requestMatchers(HttpMethod.POST, "/api/prediction/**").authenticated()
                .requestMatchers("/api/admin/**").hasRole("admin")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}