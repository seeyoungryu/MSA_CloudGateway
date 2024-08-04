# 📘 API Gateway 구현을 위한 레포지토리

### Spring Cloud Gateway, Eureka, Auth, Order, 그리고 Product 서비스로 구성된 마이크로서비스 아키텍처 구현 프로젝트
클라이언트 요청은 게이트웨이를 통해 적절한 백엔드 서비스로 라우팅되며, 각 서비스는 Eureka 서버를 통해 서비스 디스커버리 및 로드 밸런싱을 처리합니다.

### - 보안 구성
클라우드 게이트웨이의 Pre 필터에서 JWT 인증을 진행하고, Auth 서비스에서 로그인 기능을 간단하게 구현합니다. 클라우드 게이트웨이에 Pre 필터를 하나 더 생성하여 로그인을 체크합니다.

---

![서비스 구성도](https://github.com/user-attachments/assets/8da3878c-1c2b-41ca-9a07-a392a7fc37fc)

## 서비스 구성 흐름

### 1. 유저 요청

클라이언트는 `http://localhost:19091/auth/signIn?user_id=admin_1` 과 `http://localhost:19091/product` 엔드포인트로 요청을 보냅니다.

### 2. 게이트웨이

요청은 Spring Cloud Gateway (localhost:19091)를 통해 라우팅됩니다. 게이트웨이는 경로 기반 라우팅을 설정하여 적절한 서비스로 요청을 전달합니다.

### 3. Eureka 서버

게이트웨이는 Eureka 서버 (localhost:19090)를 통해 각 서비스의 인스턴스를 조회하고, 로드 밸런싱을 수행합니다.

### 4. 서비스 라우팅

- **Auth 서비스** (localhost:19095): 인증 관련 요청을 처리합니다.
- **Order 서비스** (localhost:19092): 주문 관련 요청을 처리합니다.
- **Product 서비스** (localhost:19093, localhost:19094): 제품 관련 요청을 처리하며, 다수의 인스턴스를 통해 로드 밸런싱을 지원합니다.

---

### <실행 방법>

1. 유레카 서버, 게이트웨이, 인증, 상품 순으로 어플리케이션을 실행합니다.
2. [http://localhost:19090](http://localhost:19090/)에 접속하여 각 인스턴스를 확인합니다.
3. 게이트웨이에서 상품을 요청해 봅니다. 401 에러가 발생하는 것을 볼 수 있습니다.
4. 게이트웨이에서 로그인을 요청하여 토큰을 발급받아봅니다.
5. 해당 토큰을 상품요청에 헤더에 넣어서 요청합니다. 요청을 통해 정상적으로 응답이 오는 것을 볼 수 있습니다.


---

## MSA 를 위한 개념정리

## 1. API 게이트웨이 (Spring Cloud Gateway)

### 1.1 API 게이트웨이 개요

#### 1.1.1 API 게이트웨이란?

- API 게이트웨이는 클라이언트의 요청을 받아 백엔드 서비스로 라우팅하고, 다양한 부가 기능을 제공하는 중간 서버입니다.
- 클라이언트와 서비스 간의 단일 진입점 역할을 하며, 보안, 로깅, 모니터링, 요청 필터링 등을 처리합니다.

#### 1.1.2 API 게이트웨이의 주요 기능

- **라우팅**: 클라이언트 요청을 적절한 서비스로 전달
- **인증 및 권한 부여**: 요청의 인증 및 권한을 검증
- **로드 밸런싱**: 여러 서비스 인스턴스 간의 부하 분산
- **모니터링 및 로깅**: 요청 및 응답을 로깅하고 모니터링
- **요청 및 응답 변환**: 요청과 응답을 변환하거나 필터링

### 1.2 Spring Cloud Gateway 개요

#### 1.2.1 Spring Cloud Gateway란?

- Spring Cloud Gateway는 Spring 프로젝트의 일환으로 개발된 API 게이트웨이로, 클라이언트 요청을 적절한 서비스로 라우팅하고 다양한 필터링 기능을 제공합니다.
- Spring Cloud Netflix 패키지의 일부로, 마이크로서비스 아키텍처에서 널리 사용됩니다.

#### 1.2.2 Spring Cloud Gateway의 주요 특징

- **동적 라우팅**: 요청의 URL 패턴에 따라 동적으로 라우팅
- **필터링**: 요청 전후에 다양한 작업을 수행할 수 있는 필터 체인 제공
- **모니터링**: 요청 로그 및 메트릭을 통해 서비스 상태 모니터링
- **보안**: 요청의 인증 및 권한 검증

### 1.3 Spring Cloud Gateway 설정

#### 1.3.1 기본 설정

Spring Cloud Gateway를 사용하려면 Spring Boot 애플리케이션에 의존성을 추가해야 합니다.

<details>
<summary>`build.gradle` 파일 예시</summary>

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.cloud:spring-cloud-starter-gateway'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
}
```
</details>

#### 1.3.2 라우팅 설정

`application.yml` 파일에서 라우팅 설정을 정의할 수 있습니다.

<details>
<summary>예시 설정 파일</summary>

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true  # 서비스 디스커버리를 통해 동적으로 라우트를 생성하도록 설정
      routes:
        - id: users-service  # 라우트 식별자
          uri: lb://users-service # 'users-service'라는 이름으로 로드 밸런싱된 서비스로 라우팅
          predicates:
            - Path=/users/** # /users/** 경로로 들어오는 요청을 이 라우트로 처리
        - id: orders-service  # 라우트 식별자
          uri: lb://orders-service  # 'orders-service'라는 이름으로 로드 밸런싱된 서비스로 라우팅
          predicates:
            - Path=/orders/** #/orders/** 경로로 들어오는 요청을 이 라우트로 처리

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```
</details>

### 1.4 Spring Cloud Gateway 필터링

#### 1.4.1 필터 종류

- **Global Filter**: 모든 요청에 대해 작동하는 필터
- **Gateway Filter**: 특정 라우트에만 적용되는 필터

#### 1.4.2 필터 구현

필터를 구현하려면 `GlobalFilter` 또는 `GatewayFilter` 인터페이스를 구현하고, `filter` 메서드를 오버라이드해야 합니다.

#### 1.4.3 필터 주요 객체

- **Mono**
    - `Mono`는 리액티브 프로그래밍에서 0 또는 1개의 데이터를 비동기적으로 처리합니다.
    - `Mono<Void>`는 아무 데이터도 반환하지 않음을 의미합니다.
- **ServerWebExchange**
    - `ServerWebExchange`는 HTTP 요청과 응답을 캡슐화한 객체입니다.
    - `exchange.getRequest()`로 HTTP 요청을 가져옵니다.
    - `exchange.getResponse()`로 HTTP 응답을 가져옵니다.
- **GatewayFilterChain**
    - `GatewayFilterChain`은 여러 필터를 체인처럼 연결합니다.
    - `chain.filter(exchange)`는 다음 필터로 요청을 전달합니다.

#### 1.4.4 필터 시점별 종류

- **Pre 필터**
    
    Pre 필터는 요청이 처리되기 전에 실행됩니다. 따라서 Pre 필터에서는 요청을 가로채고 필요한 작업을 수행한 다음, 체인의 다음 필터로 요청을 전달합니다. 이때, 추가적인 비동기 작업을 수행할 필요가 없기 때문에 `then` 메서드를 사용할 필요가 없습니다.

<details>
<summary>Pre 필터 예시</summary>

```java
@Component
public class PreFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 요청 로깅
        System.out.println("Request: " + exchange.getRequest().getPath());
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {  // 필터의 순서를 지정합니다.
        return -1;  // 필터 순서를 가장 높은 우선 순위로 설정합니다.
    }
}
```
</details>

- **Post 필터**
    
    Post 필터는 요청이 처리된 후, 응답이 반환되기 전에 실행됩니다. Post 필터에서는 체인의 다음 필터가 완료된 후에 실행되어야 하는 추가적인 작업을 수행해야 합니다. 따라서 `chain.filter(exchange)`를 호출하여 다음 필터를 실행한 후, `then` 메서드를 사용하여 응답이 완료된 후에 실행할 작업을 정의합니다.

<details>
<summary>Post 필터 예시</summary>

```java
@Component
public class PostFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // 응답 로깅
            System.out.println("Response Status: " + exchange.getResponse().getStatusCode());
        }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
```
</details>

## 2. 보안 구성 (OAuth2 + JWT)

### 2.1 보안 개요

#### 2.1.1 보안의 중요성

마이크로서비스 아키텍처에서는 각 서비스가 독립적으로 배포되고 통신하기 때문에 보안이 매우 중요합니다. 데이터 보호, 인증 및 권한 부여, 통신 암호화 등을 통해 시스템의 보안성을 확보해야 합니다.

### 2.2 OAuth2 개요

#### 2

.2.1 OAuth2란?

OAuth2는 ***토큰 기반의*** 인증 및 권한 부여 프로토콜입니다. 클라이언트 애플리케이션이 리소스 소유자의 권한을 얻어 보호된 리소스에 접근할 수 있도록 합니다. OAuth2는 네 가지 역할을 정의합니다: 리소스 소유자, 클라이언트, 리소스 서버, 인증 서버

#### 2.2.2 OAuth2의 주요 개념

- **Authorization Code Grant**: 인증 코드를 사용하여 액세스 토큰을 얻는 방식
- **Implicit Grant**: 클라이언트 애플리케이션에서 직접 액세스 토큰을 얻는 방식
- **Resource Owner Password Credentials Grant**: 사용자 이름과 비밀번호를 사용하여 액세스 토큰을 얻는 방식
- **Client Credentials Grant**: 클라이언트 애플리케이션이 자신의 자격 증명을 사용하여 액세스 토큰을 얻는 방식

### 2.3 JWT 개요

#### 2.3.1 JWT란?

JWT(JSON Web Token)는 JSON 형식의 자가 포함된 토큰으로, 클레임(claim)을 포함하여 사용자에 대한 정보를 전달합니다. JWT는 세 부분으로 구성됩니다: 헤더, 페이로드, 서명. JWT는 암호화를 통해 데이터의 무결성과 출처를 보장합니다.

### 2.4 Auth 서비스와 Gateway 통합

#### 2.4.1 Auth 서비스

로그인을 담당하는 서비스 어플리케이션을 생성합니다. 로그인을 진행하면 토큰을 발급받고 이 토큰을 사용하여 Gateway를 호출 합니다.

#### 2.4.2 Cloud Gateway

기존 게이트웨이 코드에 JWT 인증 및 auth-service 라우팅 정보를 추가합니다.

<details>
<summary>build.gradle 파일에 필요한 의존성 추가</summary>

```groovy
dependencies {
	implementation 'io.jsonwebtoken:jjwt:0.12.6'
}
```
</details>

---

### 추가 정보

#### Bearer 란?

<details>
    <summary>Bearer</summary>

`Bearer`는 OAuth 2.0 프로토콜에서 사용하는 인증 토큰 유형 중 하나로, 액세스 토큰을 통해 보호된 리소스에 접근할 수 있도록 합니다. `Bearer` 토큰은 요청 헤더에 포함되어 서버에 전달되며, 서버는 이를 검증하여 요청이 유효한지 확인합니다.

- **간단한 사용법**: 클라이언트는 서버에서 받은 `Bearer` 토큰을 HTTP 요청 헤더에 포함시키기만 하면 됩니다.
- **서버 측 검증**: 서버는 이 토큰을 검증하여 요청이 인증된 사용자의 요청인지 확인합니다. 일반적으로 토큰의 유효성, 만료 시간 등을 확인합니다.
- **보안**: `Bearer` 토큰은 HTTPS를 통해 전달되어야 합니다. 이를 통해 토큰이 전송 중에 도난당하지 않도록 보호할 수 있습니다.
</details>
