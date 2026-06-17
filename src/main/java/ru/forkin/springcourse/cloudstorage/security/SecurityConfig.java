package ru.forkin.springcourse.cloudstorage.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)throws Exception{
        http
            // 1. Доступ к путям
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/sign-up", "/api/auth/sign-in").permitAll()
                    .anyRequest().authenticated()
            )

            // 2. Отключаем ВСЁ лишнее для REST
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .csrf(csrf -> csrf.disable())

            // 3. Принудительный JSON при 401 (Требование ТЗ!)
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((req, res, authEx) -> {
                        res.setStatus(401);
                        res.setContentType("application/json");
                        res.setCharacterEncoding("UTF-8");
                        res.getWriter().write("{\"message\": \"User is not authenticated\"}");
                    })
            );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
