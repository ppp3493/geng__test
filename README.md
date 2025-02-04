# YOUR.GG 백엔드 개발자 과제

## 개요
GenG 백엔드 개발자 채용 프로세스 중 코딩 테스트 과제를 진행하였습니다. **소환사의 마지막 매치 상세 페이지 제작**을 목적으로 간단한 전적 검색 사이트를 제작해봤습니다.

## 사용 기술
- Java 21
- Spring Boot
- RESTful API 통합
- Thymeleaf를 이용한 HTML 뷰 렌더링
- JUnit 및 Mockito를 통한 단위 테스트

## 기능
- **Riot ID 검색**: 사용자가 Riot ID를 입력하여 프로필 정보를 조회할 수 있습니다.
- **전적 조회**: 사용자의 전적을 게임 타입(솔로 랭크, 자유 랭크, 일반 게임)별로 분류하여 표시합니다.
- **상세 전적 보기**: 사용자는 개별 경기를 클릭하여 상세 통계 및 플레이어 성과를 확인할 수 있습니다.
- **오류 처리**: 소환사를 찾을 수 없거나 다른 문제가 발생했을 때 유의미한 오류 메시지를 제공합니다.
- **비동기 처리**: CompletableFuture를 사용하여 논블로킹 데이터 조회를 수행하여 성능을 향상시킵니다.

## 설치 방법

### 필수 조건
- JDK 21
- Gradle
- IDE (예: IntelliJ IDEA, Eclipse)

### API 키 설정
- `src/main/resources/application.properties` 파일에 Riot API 키를 추가해야 합니다. 
- 다음 형식으로 API 키를 설정합니다:
   ```properties
   riot.api.key=YOUR_RIOT_API_KEY
   ```

### 설치 절차
1. 이 저장소를 클론합니다:
  ```
   bash
   git clone <repository-url>
  ```
 
2. 프로젝트 디렉토리로 이동합니다:
  ```
  cd YOUR.GG
  ```
3. Gradle을 사용하여 의존성을 설치합니다:
  ```
  ./gradlew build
  ```
4. 애플리케이션을 실행합니다:
  ```
  ./gradlew bootRun
  ```
5. 웹 브라우저에서 http://localhost:8080 으로 접속합니다.

