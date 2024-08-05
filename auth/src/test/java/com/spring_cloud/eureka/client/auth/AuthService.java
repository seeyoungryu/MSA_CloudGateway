package com.spring_cloud.eureka.client.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;


/**
 * <전체 흐름>
 * - AuthService 클래스는 JWT를 생성하는 역할을 하는 Spring 서비스 클래스입니다.
 * 클래스가 생성될 때, @Value 어노테이션을 사용하여 application.properties 파일에서 필요한 값을 주입받습니다.
 * 생성자를 통해 Base64 URL로 인코딩된 비밀 키를 디코딩하여 SecretKey 객체를 생성합니다.
 * <p>
 * - createAccessToken 메서드는 사용자 ID를 받아 JWT 액세스 토큰을 생성합니다.
 * JWT 빌더를 사용하여 사용자 ID, 역할, 발행자, 발행 시간, 만료 시간을 설정합니다.
 * SecretKey를 사용하여 HMAC-SHA512 알고리즘으로 서명한 후, JWT를 문자열로 반환합니다
 **/


@Service
public class AuthService {

    private final SecretKey secretKey;   // JWT 서명을 위해 사용되는 비밀 키
    @Value("${spring.application.name}")
    private String issuer;  // JWT 토큰의 "발행자 정보"를 담는 필드,pplication.properties 파일에서 값을 주입받습니다.
    @Value("${service.jwt.access-expiration}")
    private Long accessExpiration; //JWT 토큰의 만료 시간을 설정하는 필드, application.properties 파일에서 값을 주입받습니다.



    /* <AuthService 생성자>
    @Value 어노테이션을 사용하여 application.properties 파일에서 비밀 키 값을 주입받아 secretKey 필드를 초기화합니다.
    - Base64 URL 인코딩된 비밀 키를 디코딩하여 HMAC-SHA 알고리즘에 적합한 SecretKey 객체를 생성합니다.
    - @param secretKey Base64 URL 인코딩된 비밀 키
     */

    public AuthService(@Value("${service.jwt.secret-key}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
    }


    /* <JWT 생성 메서드>

     *
     * @param user_id 사용자 ID
     * @return 생성된 JWT 액세스 토큰
     */
    public String createAccessToken(String user_id) {  //사용자 ID를 받아 JWT 액세스 토큰을 생성
        return Jwts.builder()                          //JWT 빌더 객체를 생성합니다.
                .claim("user_id", user_id)          //JWT의 클레임에 사용자 ID를 추가합니다.
                .claim("role", "ADMIN")         //JWT의 클레임에 사용자의 역할을 추가합니다.
                .issuer(issuer)                        //.issuer(issuer): JWT 발행자를 설정합니다.
                .issuedAt(new Date(System.currentTimeMillis()))   //JWT 발행 시간을 현재 시간으로 설정
                .expiration(new Date(System.currentTimeMillis() + accessExpiration)) //JWT 만료 시간을 설정
                .signWith(secretKey, io.jsonwebtoken.SignatureAlgorithm.HS512)   //SecretKey를 사용하여 HMAC-SHA512 알고리즘으로 서명
                .compact();                             // JWT 문자열로 컴팩트하게 변환
    }
}

