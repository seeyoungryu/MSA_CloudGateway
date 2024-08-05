package com.spring_cloud.eureka.client.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * <전체 흐름>
 * 클라이언트가 /auth/signIn 경로로 GET 요청을 보냅니다. 이 요청에는 사용자 ID를 포함한 user_id 요청 매개변수가 포함됩니다.
 * createAuthenticationToken 메서드는 요청 매개변수에서 user_id를 추출하여 AuthService의 createAccessToken 메서드를 호출합니다.
 * AuthService는 사용자 ID를 바탕으로 JWT 액세스 토큰을 생성하고 반환합니다.
 * 생성된 JWT 액세스 토큰은 AuthResponse 객체로 래핑됩니다.
 * createAuthenticationToken 메서드는 AuthResponse 객체를 HTTP 200 OK 응답과 함께 클라이언트에게 반환합니다.
 */


@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    //AuthService 타입의 authService 필드를 선언하고 final 키워드로 초기화합니다. 이 필드는 JWT 토큰 생성을 담당하는 서비스입니다.


    @GetMapping("/auth/signIn")
    public ResponseEntity<?> createAuthenticationToken(@RequestParam String user_id) {
        //요청 매개변수 user_id를 메서드의 인자로 받습니다. 이 값은 클라이언트가 전달하는 사용자 ID입니다.
        return ResponseEntity.ok(new AuthResponse(authService.createAccessToken(user_id)));
        //createAuthenticationToken 메서드는 ResponseEntity<?>를 반환합니다. (: 이는 다양한 응답 상태 코드와 함께 객체를 반환할 수 있게 합니다.)
        //authService.createAccessToken(user_id): AuthService의 createAccessToken 메서드를 호출하여 JWT 액세스 토큰을 생성합니다.
        //new AuthResponse(...): 생성된 JWT 토큰을 AuthResponse 객체로 래핑합니다.
        //ResponseEntity.ok(...): HTTP 200 OK 상태 코드와 함께 AuthResponse 객체(JWT 액세스 토큰을 포함한 AuthResponse 객체)를 응답으로 반환합니다.
    }


    /*
    JWT 액세스 토큰을 포함하는 응답 객체.
     */

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class AuthResponse {
        private String access_token;
    }
}


