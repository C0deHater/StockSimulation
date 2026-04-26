# SSG - Stock Simulation Game 2026

실시간 주가 시뮬레이션 환경에서 가상 자금으로 주식 매매를 체험하는 투자 교육용 Android 앱

## 기술 스택

- **Android** : Java (AndroidX), Retrofit2, MPAndroidChart
- **Backend** : Spring Boot 4.0.5, Spring Data JPA, Spring Security
- **Database** : MySQL 8.x
- **Build** : Gradle

## 프로젝트 구조

```
SSG_Backend/       # Spring Boot 서버
SSG_Android/       # Android 클라이언트
```

## 주요 기능 (개발 예정)

- [ ] 회원가입 / 로그인
- [ ] 실시간 주가 시뮬레이션 엔진 (GBM 기반)
- [ ] 종목 리스트 조회 (실시간 갱신)
- [ ] 캔들스틱 차트
- [ ] 주식 매수 / 매도
- [ ] 포트폴리오 및 자산 관리
- [ ] 시뮬레이션 배속 / 초기화

## 실행 방법

1. MySQL에 `stock_SSG` 데이터베이스 생성
2. `SSG_Backend/src/main/resources/application.properties`에서 DB 비밀번호 수정
3. Spring Boot 서버 실행
4. Android 앱에서 `NetworkConfig.java`의 IP 주소를 서버 IP로 변경 후 실행
