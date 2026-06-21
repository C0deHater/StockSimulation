package kr.ac.dankook.StockSimulationGame2026.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 테스트를 위해 CSRF 보안 비활성화
                .authorizeHttpRequests(auth -> auth

                        .anyRequest().permitAll()
//                        .requestMatchers("/api/v1/auth/**").permitAll() // 회원가입 경로는 허용
//                        .requestMatchers("/api/v1/stocks/**").permitAll() //주식 db 정보 확인은 가능하도록
//                        .anyRequest().authenticated()
                );
        return http.build();
    }
}