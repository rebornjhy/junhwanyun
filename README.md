# 예제. 도서 관리

본 예제는 MSA/DDD/Event Storming/EDA 를 포괄하는 분석/설계/구현/운영 전단계를 커버하도록 구성한 예제입니다.
이는 클라우드 네이티브 애플리케이션의 개발에 요구되는 체크포인트들을 통과하기 위한 예시 답안을 포함합니다.
- 체크포인트 : https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW


# Table of contents

- [예제 - 도서 관리](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)
  - [신규 개발 조직의 추가](#신규-개발-조직의-추가)

# 서비스 시나리오

* 기존 도서 예약에서 사서가 도서를 입고, 재판, 목록 조회 기능 추가 - 참조. https://github.com/rebornjhy/book

### 기능적 요구사항
1. 사서가 도서를 입고한다.
1. 입고가 되면 도서 수량이 입고 수량만큼 증가한다.
1. 도서가 100권 초행으로 재판(리뉴얼)된다.
1. 재판이 되면 도서 버전이 업데이트되고 수량이 100으로 변경된다.
1. 사서는 도서 목록을 조회한다. 

### 비기능적 요구사항
1. 트랜잭션
    1. 도서를 입고하면 반드시 도서 수량이 증가해야 한다. (Req/Res)
1. 장애 격리
    1. 도서 시스템 중단 시에도 도서 목록 조회가 가능애야 한다. (Async, Eventual Consistency, Circuit Breaker)
1. 성능
    1. 사서가 도서 목록 조회에도 도서 관리의 성능 저하가 최소화되어야 한다. (CQRS)


# 체크포인트

* 분석 설계

  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리

    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?

  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?

  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?

  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?

  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
    - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?

- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
    - 모니터링, 앨럿팅: 

  - 무정지 운영 CI/CD (10)
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 
    - Contract Test :  자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?

# 분석/설계

## AS-IS 조직 (Horizontally-Aligned)
  ![image](https://user-images.githubusercontent.com/487999/79684144-2a893200-826a-11ea-9a01-79927d3a0107.png)

## TO-BE 조직 (Vertically-Aligned)
  ![image](https://user-images.githubusercontent.com/487999/79684159-3543c700-826a-11ea-8d5f-a3fc0c4cad87.png)


## Event Storming 결과
* MSAEZ 모델링 이벤트스토밍 결과: http://www.msaez.io/#/storming/mE0rA9pV1tPfOibSknVbRBhRqkY2/mine/ccf36caac98aab7713fb43c28040d31f


<img width="795" alt="스크린샷 2021-02-25 오전 1 15 36" src="https://user-images.githubusercontent.com/34236968/109101253-bbbe1480-7769-11eb-9cc2-c89b18540051.png">

### 완성된 1차 모형(팀)

![image](https://user-images.githubusercontent.com/34236968/109030662-55a0a580-7707-11eb-8c4e-531b5d73ba99.png)

### 모델 수정

* 도서 관리 기능에 대한 요구사항 추가(개인)

<img width="795" alt="스크린샷 2021-02-25 오전 1 15 36" src="https://user-images.githubusercontent.com/34236968/109101253-bbbe1480-7769-11eb-9cc2-c89b18540051.png">

### 요구사항 검증

##### 기능적 요구사항

1. 사서가 도서를 입고한다. (O)
1. 입고가 되면 도서 수량이 입고 수량만큼 증가한다. (O)
1. 도서가 100권 초행으로 재판(리뉴얼)된다. (O)
1. 재판이 되면 도서 버전이 업데이트되고 수량이 100으로 변경된다. (O)
1. 사서는 도서 목록을 조회한다. (O)

##### 비기능적 요구사항

1. 도서를 입고하면 반드시 도서 수량이 증가해야 한다. (Req/Res) (O)
1. 도서 시스템 중단 시에도 입고 및 재판은 가능해야 한다. (Circuit Breaker) (O)
1. 도서 시스템 중단 시에도 도서 목록 조회가 가느애야 한다. (Async, Eventual Consistency) (O)
1. 사서가 도서 목록 조회에도 도서 관리의 성능 저하가 최소화되어야 한다. (CQRS) (O)

## 헥사고날 아키텍처 다이어그램 도출

Req 구현 부분은 Rest Invoker/Adaptor 존재
Event Driven 구현 부분은 Kafka Publishier/Listener 존재
admin은 도서 목록 조회가 가능한 CQRS 구현 부분으로 Kafka Listener만 존재

![image](https://user-images.githubusercontent.com/34236968/109091382-033ba500-7758-11eb-8d35-82726949416f.png)

# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다. (각자의 포트넘버는 8081 ~ 808n 이다)

포트는 기존 팀 구현사항 reservaion, delivery, mypage 8081 ~ 4

- admin - 8086
- book - 8083 (기존 존재)
- gateway -8088 (기존 존재)
- library - 8085

```
# 팀 구현
cd reservaion
mvn spring-boot:run

cd delivery
mvn spring-boot:run 

cd mypage
mvn spring-boot:run  

# 개인 구현
cd admin
mvn spring-boot:run

cd book
mvn spring-boot:run 

cd gateway
mvn spring-boot:run  

cd library
mvn spring-boot:run
```

## DDD 의 적용

* 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 library 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다.

```java
package junhwanyun;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

import junhwanyun.external.Book;
import junhwanyun.external.BookService;

@Entity
@Table(name="Library_table")
public class Library {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long bookId;
    private Integer qty;
    private Double version;

    @PrePersist
    public void onPrePersist(){
        Warehoused warehoused = new Warehoused();
        BeanUtils.copyProperties(this, warehoused);
        warehoused.publishAfterCommit();

        Book book = new Book();

        LibraryApplication.applicationContext.getBean(BookService.class)
            .receive(book);
    }

    @PostUpdate
    public void onPostUpdate(){
        Renewed renewed = new Renewed();

        BeanUtils.copyProperties(this, renewed);
        renewed.publishAfterCommit();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getBookId() {
        return bookId;
    }
    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    public Integer getQty() {
        return qty;
    }
    public void setQty(Integer qty) {
        this.qty = qty;
    }
    public Double getVersion() {
        return version;
    }
    public void setVersion(Double version) {
        this.version = version;
    }
}
```

- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다

```java
package junhwanyun;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface LibraryRepository extends PagingAndSortingRepository<Library, Long> {
}
```

- 적용 후 REST API 테스트

```sh
# library 서비스의 입고
http localhost:8085/libraries id=1 bookId=1 qty=123 version=1.0

#####
# HTTP/1.1 201 
# Content-Type: application/json;charset=UTF-8
# Date: Wed, 24 Feb 2021 11:31:50 GMT
# Location: http://localhost:8085/libraries/1
# Transfer-Encoding: chunked
# 
# {
#     "_links": {
#         "library": {
#             "href": "http://localhost:8085/libraries/1"
#         }, 
#         "self": {
#             "href": "http://localhost:8085/libraries/1"
#         }
#     }, 
#     "bookId": 1, 
#     "qty": 123, 
#     "version": 1.0
# }
#####
# {"eventType":"Warehoused","timestamp":"20210224113146","id":1,"bookId":1,"qty":123,"version":1.0,"me":true}
#####

# library 서비스의 재판
http localhost:8085/libraries id=1 bookId=1 version=2.0

#####
# HTTP/1.1 201 
# Content-Type: application/json;charset=UTF-8
# Date: Wed, 24 Feb 2021 11:33:40 GMT
# Location: http://localhost:8085/libraries/1
# Transfer-Encoding: chunked
# 
# {
#     "_links": {
#         "library": {
#             "href": "http://localhost:8085/libraries/1"
#         }, 
#         "self": {
#             "href": "http://localhost:8085/libraries/1"
#         }
#     }, 
#     "bookId": 1, 
#     "qty": null, 
#     "version": 2.0
# }
#####
# {"eventType":"Renewed","timestamp":"20210224113340","id":1,"bookId":1,"version":2.0,"me":true}
# {"eventType":"Updated","timestamp":"20210224113340","id":1,"stock":100,"name":null,"version":2.0,"me":true}
#####

# 도서 상태 확인
http localhost:8083/books/1

#####
# HTTP/1.1 200 
# Content-Type: application/hal+json;charset=UTF-8
# Date: Wed, 24 Feb 2021 11:35:26 GMT
# Transfer-Encoding: chunked
# 
# {
#     "_links": {
#         "book": {
#             "href": "http://localhost:8083/books/1"
#         }, 
#         "self": {
#             "href": "http://localhost:8083/books/1"
#         }
#     }, 
#     "name": null, 
#     "stock": 100, 
#     "version": 2.0
# }
#####

# 도서 목록 확인
http localhost:8086/admins

#####
# HTTP/1.1 200 
# Content-Type: application/hal+json;charset=UTF-8
# Date: Wed, 24 Feb 2021 11:37:23 GMT
# Transfer-Encoding: chunked
# 
# {
#     "_embedded": {
#         "admins": [
#             {
#                 "_links": {
#                     "admin": {
#                         "href": "http://localhost:8086/admins/1"
#                     }, 
#                     "self": {
#                         "href": "http://localhost:8086/admins/1"
#                     }
#                 }, 
#                 "bookId": 1, 
#                 "bookStock": 100, 
#                 "bookVersion": 2.0
#             }
#         ]
#     }, 
#     "_links": {
#         "profile": {
#             "href": "http://localhost:8086/profile/admins"
#         }, 
#         "self": {
#             "href": "http://localhost:8086/admins"
#         }
#     }
# }
#####
```

## 폴리글랏 퍼시스턴스

도서관(library) 서비스는 폴리글랏 퍼시스턴스 적용을 위해 HSQLDB를 사용하기로 하였다. 이를 위해 pom.xml에 hsqldb 의존성 추가 후 테스트를 수행하였다. 별다른 작업없이 기존의 Entity Pattern 과 Repository Pattern 적용과 데이터베이스 제품의 설정 (application.yml) 만으로 HSQLDB 에 부착시켰다

```java
# book/pom.xml - 기존 h2 주석 처리 후 mvn clean install

        <!-- https://mvnrepository.com/artifact/org.hsqldb/hsqldb -->
        <dependency>
            <groupId>org.hsqldb</groupId>
            <artifactId>hsqldb</artifactId>
            <scope>runtime</scope>
        </dependency>

		<!-- <dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
		</dependency> -->

# BookRepository.java

package fooddelivery;

public interface BookRepository extends JpaRepository<Order, UUID>{
}
```

## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 도서관(library) -> 도서(book) 간의 입고 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다.
호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

* 결제서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```java
# (libraries) BookService.java

package junhwanyun.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="book", url="http://localhost:8083") //url="http://book:8080")
public interface BookService {

    @RequestMapping(method= RequestMethod.POST, path="/books")
    public void receive(@RequestBody Book book);

}
```

* 입고 직전(@PrePersist) 수량 증가를 요청하도록 처리
```java
# Library.java (Entity)

    @PrePersist
    public void onPrePersist(){
        Warehoused warehoused = new Warehoused();
        BeanUtils.copyProperties(this, warehoused);
        warehoused.publishAfterCommit();

        Book book = new Book();
        
        LibraryApplication.applicationContext.getBean(BookService.class)
            .receive(book);
    }
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 도서 시스템이 장애가 나면 입고 불가능 확인:

```bash
# 도서(book) 서비스를 잠시 내려놓음 (ctrl+c)

# 입고
http localhost:8085/libraries id=2 bookId=2 qty=22 version=1.0 # Fail

#####
# HTTP/1.1 500 
# Connection: close
# Content-Type: application/json;charset=UTF-8
# Date: Wed, 24 Feb 2021 11:39:43 GMT
# Transfer-Encoding: chunked
# 
# {
#     "error": "Internal Server Error", 
#     "message": "Connection refused (Connection refused) executing POST http://localhost:8083/books", 
#     "path": "/libraries", 
#     "status": 500, 
#     "timestamp": "2021-02-24T11:39:43.914+0000"
# }
#####

# 도서 서비스 재기동
cd book
mvn spring-boot:run

# 입고
http localhost:8085/libraries id=3 bookId=3 qty=333 version=1.0

#####
# HTTP/1.1 201 
# Content-Type: application/json;charset=UTF-8
# Date: Wed, 24 Feb 2021 11:42:21 GMT
# Location: http://localhost:8085/libraries/2
# Transfer-Encoding: chunked
# 
# {
#     "_links": {
#         "library": {
#             "href": "http://localhost:8085/libraries/2"
#         }, 
#         "self": {
#             "href": "http://localhost:8085/libraries/2"
#         }
#     }, 
#     "bookId": 3, 
#     "qty": 333, 
#     "version": 1.0
# }
#####

http localhost:8085/libraries id=4 bookId=4 qty=4444 version=1.0

#####
# Content-Type: application/json;charset=UTF-8
# Date: Wed, 24 Feb 2021 11:42:25 GMT
# Location: http://localhost:8085/libraries/3
# Transfer-Encoding: chunked
# 
# {
#     "_links": {
#         "library": {
#             "href": "http://localhost:8085/libraries/3"
#         }, 
#         "self": {
#             "href": "http://localhost:8085/libraries/3"
#         }
#     }, 
#     "bookId": 4, 
#     "qty": 4444, 
#     "version": 1.0
# }
#####
```

- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)

## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

재판 후에 도서 시스템으로 이를 알려주는 행위는 동기식이 아니라 비 동기식으로 처리하여 도서 시스템의 처리를 위하여 재판이 블로킹 되지 않아도록 처리한다.
 
- 이를 위하여 재판 기록을 남긴 후에 곧바로 재판이 되었다는 도메인 이벤트를 카프카로 송출한다 (Publish)
 
```java
    @PostUpdate
    public void onPostUpdate(){
        Renewed renewed = new Renewed();
        BeanUtils.copyProperties(this, renewed);
        renewed.publishAfterCommit();
    }
```
- 도서 서비스에서는 결제승인 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다: 100권 초행으로 기능적 요구사항에 명시

```java
@   StreamListener(KafkaProcessor.INPUT)
    public void wheneverRenewed_(@Payload Renewed renewed){

        if(renewed.isMe()){
            System.out.println("##### listener  : " + renewed.toJson());
            //
            Book book = bookRepository.findById(renewed.getBookId()).get();

            book.setId(renewed.getBookId());
            book.setVersion(renewed.getVersion());
            book.setStock(100); // 100권 입고

            bookRepository.save(book);
        }
    }
```

# 운영

## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

시나리오는 도서관(library) -> 도서(book) 입고를 RESTful Request/Response 로 연동하여 구현이 되어있고, 압고 요청이 과도할 경우 CB 를 통하여 장애격리.

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 777 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정

```yml
# application.yml

feign:
  hystrix:
    enabled: true
    
hystrix:
  command:
    default:
      execution.isolation.thread.timeoutInMilliseconds: 777
```

- 피호출 서비스(도서: book) 임의 부하 처리 - 777ms

```java
# Book.java - 입고 요청 시 도서가 response 해야하므로, 도서에 Tread.sleep() - PrePersist 변경

    @PrePersist
    public void onPrePersist() throws InterruptedException {
        Thread.sleep((long) (444 + Math.random() * 444)); // 444ms +-444ms delay
        
        ...
    }
```

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 255명
- 60초 동안 실시
- 강사님께서 말씀주신 대로 너무 단순한 구조여서,, Failed transactions 5 / Availability 99.42%...(?)

```sh
# kubectl exec -ti pod/siege-5c7c46b788-kxxrt /bin/bash - 잘못된 정보로 우선 로컬 테스트

siege -c100 -t60S -r10 -v --content-type "application/json" 'http://localhost:8083/library POST {"qty": 1234, "version": 1.0}'

#####
# ...
#
# HTTP/1.1 500     0.03 secs:     190 bytes ==> POST http://localhost:8085/libraries # Fali
# HTTP/1.1 500     0.01 secs:     190 bytes ==> POST http://localhost:8085/libraries # Fali
# HTTP/1.1 500     0.02 secs:     190 bytes ==> POST http://localhost:8085/libraries # Fali
# HTTP/1.1 500     0.02 secs:     190 bytes ==> POST http://localhost:8085/libraries # Fali
# HTTP/1.1 500     0.03 secs:     190 bytes ==> POST http://localhost:8085/libraries # Fali
# HTTP/1.1 201     0.80 secs:     226 bytes ==> POST http://localhost:8085/libraries
# HTTP/1.1 500     0.83 secs:     184 bytes ==> POST http://localhost:8085/libraries # Fail
# HTTP/1.1 500     0.03 secs:     208 bytes ==> POST http://localhost:8085/libraries # Fail
# HTTP/1.1 500     0.03 secs:     208 bytes ==> POST http://localhost:8085/libraries # Fail
# HTTP/1.1 500     0.03 secs:     208 bytes ==> POST http://localhost:8085/libraries # Fail
# HTTP/1.1 500     0.04 secs:     208 bytes ==> POST http://localhost:8085/libraries # Fail
# HTTP/1.1 500     0.03 secs:     208 bytes ==> POST http://localhost:8085/libraries # Fail
# HTTP/1.1 500     0.03 secs:     208 bytes ==> POST http://localhost:8085/libraries # Fail
# HTTP/1.1 500     0.02 secs:     208 bytes ==> POST http://localhost:8085/libraries # Fail
# HTTP/1.1 500     0.02 secs:     208 bytes ==> POST http://localhost:8085/libraries # Fail
# HTTP/1.1 201     0.85 secs:     226 bytes ==> POST http://localhost:8085/libraries
# HTTP/1.1 201     0.86 secs:     226 bytes ==> POST http://localhost:8085/libraries
# HTTP/1.1 500     0.02 secs:     208 bytes ==> POST http://localhost:8085/libraries # Fail
# HTTP/1.1 500     0.03 secs:     208 bytes ==> POST http://localhost:8085/libraries # Fail
# HTTP/1.1 500     0.02 secs:     208 bytes ==> POST http://localhost:8085/libraries # Fail
# HTTP/1.1 500     0.02 secs:     208 bytes ==> POST http://localhost:8085/libraries # Fail
# HTTP/1.1 500     0.96 secs:     184 bytes ==> POST http://localhost:8085/libraries # Fail
# HTTP/1.1 500     1.01 secs:     184 bytes ==> POST http://localhost:8085/libraries # Fail
# HTTP/1.1 201     0.56 secs:     226 bytes ==> POST http://localhost:8085/libraries
# HTTP/1.1 201     0.50 secs:     226 bytes ==> POST http://localhost:8085/libraries
# HTTP/1.1 201     0.52 secs:     226 bytes ==> POST http://localhost:8085/libraries
# HTTP/1.1 201     0.56 secs:     226 bytes ==> POST http://localhost:8085/libraries
# HTTP/1.1 201     0.59 secs:     226 bytes ==> POST http://localhost:8085/libraries
# HTTP/1.1 201     0.57 secs:     226 bytes ==> POST http://localhost:8085/libraries
# HTTP/1.1 500     0.80 secs:     184 bytes ==> POST http://localhost:8085/libraries # Fail
# 
# ...
# 
# Lifting the server siege...
# Transactions:                     74 hits
# Availability:                   6.68 %
# Elapsed time:                  10.11 secs
# Data transferred:               0.21 MB
# Response time:                  8.75 secs
# Transaction rate:               7.32 trans/sec
# Throughput:                     0.02 MB/sec
# Concurrency:                   64.08
# Successful transactions:          74
# Failed transactions:            1033
# Longest transaction:            2.23
# Shortest transaction:           0.00
#####
```
- 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여줌. 하지만, % 가 성공하였고, %가 실패했다는 것은 고객 사용성에 있어 좋지 않기 때문에 Retry 설정과 동적 Scale out(HPA)을 통하여 시스템을 확장 해주는 후속처리가 필요.

- Availability 가 높아진 것을 확인 (siege)

### 오토스케일 아웃

앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다. 

- HPA TARGET <unknown> 이슈 트러블슈팅 + 메트릭 서버...(?)

```sh
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

- 이전 부하 없이 바로 테스트를 위해 10m으로 세팅했으나, 강사님 Feedback으로 200m 세팅

```yml
# book/kubernetes/deployment.yml

          resources:
            requests:
              cpu: "200m"
            limits:
              cpu: "500m"

kubectl apply -f deployment.yml
```

- 도서 서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 7%를 넘어서면 replica 를 7개까지 늘려준다:

```sh
kubectl autoscale deploy book --min=1 --max=7 --cpu-percent=7

kubectl get hpa

#####
# NAME                                       REFERENCE         TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
# horizontalpodautoscaler.autoscaling/book   Deployment/book   35%/7%    1         7         4          18m
#####
```

- CB 에서 했던 방식대로 워크로드를 1분 동안 걸어준다.
- 성공률이 원래 높았는데, 줄었다...(?)

```sh
kubectl exec -ti pod/siege-5c7c46b788-kxxrt /bin/bash

siege -c255 -t60S -r10 -v --content-type "application/json" 'http://book:8080/books POST {"stock": 1234, "version": 1.0}'

#####
# Lifting the server siege...
# Transactions:                    709 hits
# Availability:                  87.64 %
# Elapsed time:                  59.99 secs
# Data transferred:               0.14 MB
# Response time:                 13.39 secs
# Transaction rate:              11.82 trans/sec
# Throughput:                     0.00 MB/sec
# Concurrency:                  158.28
# Successful transactions:         709
# Failed transactions:             100
# Longest transaction:           31.70
# Shortest transaction:           0.54
#####
```

```
siege -c255 -t60S -r10 -v --content-type "application/json" 'http://library:8080/libraries POST {"bookId": 1, "version": 1.0}'
```

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:

```sh
kubectl get deploy book -w

#####
# NAME          READY   UP-TO-DATE   AVAILABLE   AGE
# book          7/7     7            7           18m
#####
```

## 무정지 재배포

* Zero-downtime deploy(Readiness Probe)는 유효하지 않은 path에 적용해 failed를 확인하였다.

```yml
# book/kubernetes/deployment.yml

          readinessProbe:
            httpGet:
              path: '/readinessProbe'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10

kubectl get po

#####
# NAME                       READY   STATUS    RESTARTS   AGE
# book-79b58678d8-r77jb      0/1     Running   0          6m54s
#####

kubectl describe pod/book-79b58678d8-r77jb

#####
# Name:         book-79b58678d8-r77jb
# Namespace:    default
# Priority:     0
# Node:         ip-192-168-80-198.ap-northeast-1.compute.internal/192.168.80.198
# Start Time:   Wed, 24 Feb 2021 23:19:33 +0900
# Labels:       app=book
#               pod-template-hash=79b58678d8
# Annotations:  kubernetes.io/psp: eks.privileged
# Status:       Running
# IP:           192.168.86.129
# IPs:
#   IP:           192.168.86.129
# Controlled By:  ReplicaSet/book-79b58678d8
# Containers:
#   book:
#     Container ID:   docker://e4f458d6b9f0eac02b8385bbaa3a4dc1047ca5c2e04d414cb053b152ac9ed8e6
#     Image:          496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-book:latest
#     Image ID:       docker-pullable://496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-book@sha256:61afb6052368e18777ab9ad3bd299c855952dc387b35053e02167dd57fbc626b
#     Port:           8080/TCP
#     Host Port:      0/TCP
#     State:          Running
#       Started:      Wed, 24 Feb 2021 23:19:38 +0900
#     Ready:          False
#     Restart Count:  0
#     Limits:
#       cpu:  10m
#     Requests:
#       cpu:        10m
#     Readiness:    http-get http://:8080/actuator/health delay=10s timeout=2s period=5s #success=1 #failure=10
#     Environment:  <none>
#     Mounts:
#       /var/run/secrets/kubernetes.io/serviceaccount from default-token-6z55n (ro)
# Conditions:
#   Type              Status
#   Initialized       True 
#   Ready             False 
#   ContainersReady   False 
#   PodScheduled      True 
# Volumes:
#   default-token-6z55n:
#     Type:        Secret (a volume populated by a Secret)
#     SecretName:  default-token-6z55n
#     Optional:    false
# QoS Class:       Burstable
# Node-Selectors:  <none>
# Tolerations:     node.kubernetes.io/not-ready:NoExecute for 300s
#                  node.kubernetes.io/unreachable:NoExecute for 300s
# Events:
#   Type     Reason     Age                From                                                        Message
#   ----     ------     ----               ----                                                        -------
#   Normal   Scheduled  65s                default-scheduler                                           Successfully assigned default/book-79b58678d8-r77jb to ip-192-168-80-198.ap-northeast-1.compute.internal
#   Normal   Pulling    63s                kubelet, ip-192-168-80-198.ap-northeast-1.compute.internal  Pulling image "496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-book:latest"
#   Normal   Pulled     62s                kubelet, ip-192-168-80-198.ap-northeast-1.compute.internal  Successfully pulled image "496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-book:latest"
#   Normal   Created    62s                kubelet, ip-192-168-80-198.ap-northeast-1.compute.internal  Created container book
#   Normal   Started    60s                kubelet, ip-192-168-80-198.ap-northeast-1.compute.internal  Started container book
#   Warning  Unhealthy  4s (x10 over 49s)  kubelet, ip-192-168-80-198.ap-northeast-1.compute.internal  Readiness probe failed: Get http://192.168.86.129:8080/actuator/health: dial tcp 192.168.86.129:8080: connect: connection refused
#####
```

* Self-healing(Liveness Probe) 역시 유효하지 않은 path에 적용해 failed를 확인하였다.

```yml
# book/kubernetese/deployment.yml

          livenessProbe:
            httpGet:
              path: '/livenessProbe'
              port: 8080
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5

kubectl get po

#####
# NAME                       READY   STATUS    RESTARTS   AGE
# book-74ff69c667-qj745      1/1     Running   1          4m9s
#####

kubectl describe po book-74ff69c667-qj745

#####
# Name:         book-74ff69c667-qj745
# Namespace:    default
# Priority:     0
# Node:         ip-192-168-80-198.ap-northeast-1.compute.internal/192.168.80.198
# Start Time:   Wed, 24 Feb 2021 23:39:28 +0900
# Labels:       app=book
#               pod-template-hash=74ff69c667
# Annotations:  kubernetes.io/psp: eks.privileged
# Status:       Running
# IP:           192.168.84.159
# IPs:
#   IP:           192.168.84.159
# Controlled By:  ReplicaSet/book-74ff69c667
# Containers:
#   book:
#     Container ID:   docker://72b2126f62a5ccf39b9b6ec413c1a41e72336ed2564049f5bd8881e3d3574302
#     Image:          496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-book:latest
#     Image ID:       docker-pullable://496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-book@sha256:61afb6052368e18777ab9ad3bd299c855952dc387b35053e02167dd57fbc626b
#     Port:           8080/TCP
#     Host Port:      0/TCP
#     State:          Running
#       Started:      Wed, 24 Feb 2021 23:41:50 +0900
#     Last State:     Terminated
#       Reason:       Error
#       Exit Code:    143
#       Started:      Wed, 24 Feb 2021 23:39:29 +0900
#       Finished:     Wed, 24 Feb 2021 23:41:50 +0900
#     Ready:          True
#     Restart Count:  1
#     Liveness:       http-get http://:8080/livenessProbe delay=120s timeout=2s period=5s #success=1 #failure=5
#     Environment:    <none>
#     Mounts:
#       /var/run/secrets/kubernetes.io/serviceaccount from default-token-6z55n (ro)
# Conditions:
#   Type              Status
#   Initialized       True 
#   Ready             True 
#   ContainersReady   True 
#   PodScheduled      True 
# Volumes:
#   default-token-6z55n:
#     Type:        Secret (a volume populated by a Secret)
#     SecretName:  default-token-6z55n
#     Optional:    false
# QoS Class:       BestEffort
# Node-Selectors:  <none>
# Tolerations:     node.kubernetes.io/not-ready:NoExecute for 300s
#                  node.kubernetes.io/unreachable:NoExecute for 300s
# Events:
#   Type     Reason     Age                   From                                                        Message
#   ----     ------     ----                  ----                                                        -------
#   Normal   Scheduled  4m29s                 default-scheduler                                           Successfully assigned default/book-74ff69c667-qj745 to ip-192-168-80-198.ap-northeast-1.compute.internal
#   Normal   Pulling    2m7s (x2 over 4m28s)  kubelet, ip-192-168-80-198.ap-northeast-1.compute.internal  Pulling image "496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-book:latest"
#   Normal   Pulled     2m7s (x2 over 4m28s)  kubelet, ip-192-168-80-198.ap-northeast-1.compute.internal  Successfully pulled image "496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-book:latest"
#   Normal   Created    2m7s (x2 over 4m28s)  kubelet, ip-192-168-80-198.ap-northeast-1.compute.internal  Created container book
#   Normal   Started    2m7s (x2 over 4m28s)  kubelet, ip-192-168-80-198.ap-northeast-1.compute.internal  Started container book
#   Normal   Killing    2m7s                  kubelet, ip-192-168-80-198.ap-northeast-1.compute.internal  Container book failed liveness probe, will be restarted
#   Warning  Unhealthy  2s (x7 over 2m27s)    kubelet, ip-192-168-80-198.ap-northeast-1.compute.internal  Liveness probe failed: HTTP probe failed with statuscode: 404
#####
```

# + 팀 구현 때 못했던 것들..

## configmap

configmap 책 이름 해리포터 name=HarryPorter 매핑

```sh
kubectl create cm book --from-literal=name=HarryPorter

kubectl get cm book -o yaml

#####
# apiVersion: v1
# data:
#   name: HarryPorter
# kind: ConfigMap
# metadata:
#   creationTimestamp: "2021-02-24T15:16:21Z"
#   name: book
#   namespace: default
#   resourceVersion: "90319"
#   selfLink: /api/v1/namespaces/default/configmaps/book
#   uid: b7009b31-35d2-4137-a676-88d6d56532f0
#####
```

book 서비스 환경변수에 책 이름 설정

```yml
# book/kubernetes/deployment.yml

          env:
            - name: NAME
              valueFrom:
                configMapKeyRef:
                  name: book
                  key: name

# book pod의 env에 등록 확인
kubectl exec -ti book-7df9fb6c98-vtwdm /bin/sh

env | grep HarryPorter

#####
# NAME=HarryPorter
#####
```
