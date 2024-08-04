# API 게이트웨이 구현을 위한 레파지토리

클라우드 게이트웨이 + 유레카 + Order 인스턴스(1개) + Product 인스턴스(2개) 로 진행

![image](https://github.com/user-attachments/assets/8da3878c-1c2b-41ca-9a07-a392a7fc37fc)

## 1. 실행 방법

1. 유레카 서버 ⇒ 게이트웨이 ⇒ 주문 ⇒ 상품 순으로 어플리케이션을 실행합니다.
2. [http://localhost:19090](http://localhost:19090/)에 접속하여 각 인스턴스를 확인합니다.
3. [http://localhost:19091/order](http://localhost:19091/order) 로 접속하여 게이트웨이에서 order 서비스를 호출하는 것을 확인할 수 있습니다.
4. [http://localhost:19091/product](http://localhost:19091/product) 를 여러 번 호출하면서 포트가 달라지는 것을 확인합니다. 이를 통해 로드밸런싱이 동작함을 확인합니다.
5. 게이트웨이의 로그를 보면 호출할 때마다 필터가 동작하는 것을 확인할 수 있습니다.

---

# 2. API 게이트웨이 (Spring Cloud Gateway)

## 2.1 API 게이트웨이 개요

### 2.1.1 API 게이트웨이란?

- API 게이트웨이는 클라이언트의 요청을 받아 백엔드 서비스로 라우팅하고, 다양한 부가 기능을 제공하는 중간 서버입니다.
- 클라이언트와 서비스 간의 단일 진입점 역할을 하며, 보안, 로깅, 모니터링, 요청 필터링 등을 처리합니다.

### 2.1.2 API 게이트웨이의 주요 기능

- **라우팅**: 클라이언트 요청을 적절한 서비스로 전달
- **인증 및 권한 부여**: 요청의 인증 및 권한을 검증
- **로드 밸런싱**: 여러 서비스 인스턴스 간의 부하 분산
- **모니터링 및 로깅**: 요청 및 응답을 로깅하고 모니터링
- **요청 및 응답 변환**: 요청과 응답을 변환하거나 필터링

## 2.2 Spring Cloud Gateway 개요

### 2.2.1 Spring Cloud Gateway란?

- Spring Cloud Gateway는 Spring 프로젝트의 일환으로 개발된 API 게이트웨이로, 클라이언트 요청을 적절한 서비스로 라우팅하고 다양한 필터링 기능을 제공합니다.
- Spring Cloud Netflix 패키지의 일부로, 마이크로서비스 아키텍처에서 널리 사용됩니다.

### 2.2.2 Spring Cloud Gateway의 주요 특징

- **동적 라우팅**: 요청의 URL 패턴에 따라 동적으로 라우팅
- **필터링**: 요청 전후에 다양한 작업을 수행할 수 있는 필터 체인 제공
- **모니터링**: 요청 로그 및 메트릭을 통해 서비스 상태 모니터링
- **보안**: 요청의 인증 및 권한 검증

## 2.3 Spring Cloud Gateway 설정

### 2.3.1 기본 설정

- Spring Cloud Gateway를 사용하려면 Spring Boot 애플리케이션에 의존성을 추가해야 합니다.

<details>
<summary> `build.gradle` 파일 예시 </summary>

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.cloud:spring-cloud-starter-gateway'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
}
```
</details>

### 2.3.2 라우팅 설정

- `application.yml` 파일에서 라우팅 설정을 정의할 수 있습니다.

<details> <summary>예시 설정 파일</summary>

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

## 2.4 Spring Cloud Gateway 필터링

### 2.4.1 필터 종류

- **Global Filter**: 모든 요청에 대해 작동하는 필터
- **Gateway Filter**: 특정 라우트에만 적용되는 필터

### 2.4.2 필터 구현

- 필터를 구현하려면 `GlobalFilter` 또는 `GatewayFilter` 인터페이스를 구현하고, `filter` 메서드를 오버라이드해야 합니다.

### 2.4.3 필터 주요 객체

- Mono
    - `Mono`는 리액티브 프로그래밍에서 0 또는 1개의 데이터를 비동기적으로 처리합니다.
    - `Mono<Void>`는 아무 데이터도 반환하지 않음을 의미합니다.
- ServerWebExchange
    - `ServerWebExchange`는 HTTP 요청과 응답을 캡슐화한 객체입니다.
    - `exchange.getRequest()`로 HTTP 요청을 가져옵니다.
    - `exchange.getResponse()`로 HTTP 응답을 가져옵니다.
- GatewayFilterChain
    - `GatewayFilterChain`은 여러 필터를 체인처럼 연결합니다.
    - `chain.filter(exchange)`는 다음 필터로 요청을 전달합니다.

### 2.4.4 필터 시점별 종류

- Pre 필터

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

- Post 필터

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

## 2.5 Spring Cloud와의 통합

### 2.5.1 Spring Cloud와의 통합

- Spring Cloud Gateway는 Spring Cloud Netflix 패키지의 일부로, Eureka와 쉽게 통합할 수 있습니다.
- Eureka를 통해 동적으로 서비스 인스턴스를 조회하여 로드 밸런싱과 라우팅을 수행할 수 있습니다.
