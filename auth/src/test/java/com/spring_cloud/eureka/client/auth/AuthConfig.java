package com.spring_cloud.eureka.client.auth;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration    //@Configuration 어노테이션은 이 AuthConfig 클래스가 Spring의 보안 <설정>을 정의하는 클래스임을 나타냅니다.
@EnableWebSecurity
public class AuthConfig {


    /* SecurityFilterChain 빈을 정의합니다.
        이 메서드는 Spring Security의 보안 필터 체인을 구성합니다.

    1. 보안 필터 체인을 설정하고 SecurityFilterChain 객체를 반환하는 Spring Bean을 정의합니다.

    */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())   //CSRF 보호 -> 비활성화
                .authorizeRequests(authorize -> authorize
                        .requestMatchers("/auth/signIn").permitAll()   //해당 경로 -> permitAll
                        .anyRequest().authenticated()        //그 외 경로 -> 인증 필요함
                )

                // 세션 관리 정책을 정의합니다. 여기서는 세션을 사용하지 않도록(STATELESS) 설정합니다.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );


        // 설정된 보안 필터 체인을 반환합니다.
        //http.build()는 HttpSecurity 객체를 빌드하여 SecurityFilterChain 객체를 생성합니다.
        return http.build();
    }
}