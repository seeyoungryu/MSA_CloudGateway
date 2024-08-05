package com.spring_cloud.eureka.client.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;


/**
 * v <전체 흐름>
 * 클라이언트가 요청을 보냅니다.
 * LocalJwtAuthenticationFilter는 모든 요청을 가로채고 필터링을 시작합니다.
 * 요청 경로가 /auth/signIn인 경우 필터를 적용하지 않고 요청을 통과시킵니다.
 * extractToken 메서드를 사용하여 요청 헤더에서 JWT 토큰을 추출합니다.
 * validateToken 메서드를 사용하여 토큰의 유효성을 검증합니다.
 * 토큰이 유효하지 않으면 HTTP 401 Unauthorized 응답을 반환합니다.
 * 토큰이 유효하면 다음 필터로 요청을 전달합니다.
 */


@Slf4j  //Lombok 라이브러리 ~ 로깅 기능 추가
@Component
public class LocalJwtAuthenticationFilter implements GlobalFilter {
    //GlobalFilter: Spring Cloud Gateway에서 모든 요청에 대해 적용되는 전역 필터를 구현합니다.


    @Value("${service.jwt.secret-key}") //application.properties 파일에서 JWT 비밀 키를 주입받습니다.
    private String secretKey;



    /*
    필터 메서드
     */

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath(); //요청 경로를 가져옵니다.
        if (path.equals("/auth/signIn")) {
            return chain.filter(exchange);  // /signIn 경로는 필터를 적용하지 않음
        }

        String token = extractToken(exchange); // 요청에서 JWT 토큰을 추출합니다.

        if (token == null || !validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED); ///토큰이 없거나 유효하지 않으면 HTTP 401 Unauthorized 상태를 설정하고 응답을 완료합니다.
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange); // 요청이 유효한 경우 다음 필터로 요청을 전달합니다.
    }



    /*
    토큰 추출 메서드
     */

    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }



    /*
    JWT 토큰 검증 유효성 검증 메서드
     */

    private boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));   // 키 생성
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(key)
                    .build().parseSignedClaims(token);       //토큰을 파싱하고 검증

            log.info("#####payload :: " + claimsJws.getPayload().toString());   //토큰의 페이로드 정보를 로그에 출력

            // 추가적인 검증 로직 (예: 토큰 만료 여부 확인 등)을 여기에 추가할 수 있음


            return true;  // 토큰이 유효한 경우 true를 반환합니다.
        } catch (Exception e) {
            return false; //예외가 발생하면 false를 반환합니다.
        }
    }


}