# 학교 시설/비품 예약 관리 시스템

학교 내 시설 및 비품을 온라인으로 예약하고 관리할 수 있는 Java 콘솔 기반 예약 관리 시스템입니다.  
관리자 권한별로 메뉴가 분리되어 있으며, TCP 소켓을 활용한 실시간 알림 기능을 제공합니다.

---

## 기술 스택

| 항목 | 내용 |
|------|------|
| Language | Java 17 |
| DB | MySQL 8.x |
| JDBC | 직접 연결 관리 (commit / rollback 수동) |
| 라이브러리 | Lombok (Builder, Getter, Setter) |
| 알림 | TCP Socket (port 9999) + ScheduledExecutorService |
| 컨테이너 | Docker / docker-compose |
| IDE | IntelliJ IDEA |

---

## 실행 방법

### 1. DB 실행 (Docker)

```bash
docker-compose up -d
```

> `docker/init.sql`이 자동으로 실행되어 테이블 및 더미데이터가 초기화됩니다.

### 2. 서버 실행

```
ServerMain.java 실행
```

알림 소켓 서버가 **9999 포트**로 대기합니다.

### 3. 클라이언트 실행

```
ClientMain.java 실행
```

---

## 프로젝트 구조

```
src/com/kimdoolim/
├── main/
│   ├── ClientMain.java          # 클라이언트 진입점 (로그인 → 권한별 메뉴)
│   └── ServerMain.java          # 알림 소켓 서버
│
├── view/                        # 사용자 화면 (콘솔 UI)
│   ├── LoginView.java
│   ├── MainView.java            # 권한별 메인 메뉴 분기
│   ├── ReservationView.java     # 예약 신청 / 내역 조회 / 반납
│   ├── UserManageView.java      # 사용자 관리 (상위 관리자 전용)
│   └── AlarmView.java           # 알림 조회
│
├── manager/                     # 중간/상위 관리자 전용 화면
│   ├── ManagerReservationView.java   # 예약 승인 / 반려
│   ├── FacilityEquipmentView.java    # 시설/비품 관리
│   ├── BlockScheduleView.java        # 제한 스케줄 관리
│   ├── BlockPeriodManageView.java    # 제한 기간 관리
│   └── BlockPeriodController.java    # 교시 등록 컨트롤러 (싱글톤)
│
├── service/                     # 비즈니스 로직
│   ├── ReservationService.java
│   ├── FacilityEquipmentService.java
│   ├── UserService.java
│   └── BlockScheduleService.java
│
├── dao/                         # DB 접근 계층
│   ├── ReservationDAO.java
│   ├── FacilityEquipmentDAO.java
│   ├── UserDAO.java
│   └── BlockScheduleDAO.java
│
├── dto/                         # 데이터 전송 객체
│   ├── User.java
│   ├── Facility.java
│   ├── Equipment.java
│   ├── EquipmentDetail.java
│   ├── Reservation.java
│   ├── Alarm.java
│   ├── Period.java
│   ├── BlockPeriod.java
│   └── Permission.java          # enum: ADMIN / MIDDLEADMIN / USER
│
├── alarm/                       # 실시간 알림 시스템
│   ├── AlarmService.java        # 알림 DB 처리 (싱글톤)
│   ├── AlarmSendingManager.java # 소켓 전송 관리 (싱글톤)
│   ├── AlarmScheduler.java      # 예약 시작 알림 스케줄러
│   └── AlarmReceiveThread.java  # 클라이언트 수신 스레드
│
├── socket/
│   └── SocketSession.java       # 서버측 클라이언트 세션 처리
│
└── common/
    ├── AppScanner.java           # Scanner 싱글톤
    ├── Auth.java                 # 로그인 세션 (현재 유저 정보)
    ├── Database.java
    └── MySql.java                # JDBC 연결 / commit / rollback / close
```

---

## 권한 체계

| 권한 | 설명 |
|------|------|
| `ADMIN` | 상위 관리자 - 전체 메뉴 접근 가능 |
| `MIDDLEADMIN` | 중간 관리자 - 예약 승인/반려, 시설 관리, 제한 기간 관리 |
| `USER` | 일반 사용자 - 예약 신청, 내역 조회, 알림 확인 |

---

## 주요 기능

### 공통 (로그인 후 전체 권한)
- 예약 신청 (시설 / 비품)
- 예약 내역 조회 및 취소
- 비품 반납 신청
- 마이페이지 (비밀번호 변경 등)
- 실시간 알림 수신 및 조회

### 중간 관리자 추가 기능
- 예약 승인 / 반려
- 시설 및 비품 등록 / 수정 / 삭제
- 비품 낱개 상태 관리 (정상 / 수리중 / 점검중 등)
- 제한 스케줄 관리 (특정 기간 예약 불가 설정)
- 제한 기간 관리

### 상위 관리자 추가 기능
- 사용자 등록
- 사용자 상태 변경 (휴직 / 복직 / 전근)
- 권한 변경 (USER ↔ MIDDLEADMIN)
- 제한 기간 관리

---

## 알림 시스템

TCP 소켓(포트 9999) 기반 실시간 알림 시스템입니다.

```
[서버] ServerMain (port 9999)
   └── SocketSession (클라이언트별 세션)
          └── AlarmSendingManager → 연결된 클라이언트에 메시지 전송

[클라이언트] AlarmReceiveThread
   └── 로그인 후 백그라운드 수신 대기
   └── 메시지 수신 시 콘솔에 즉시 출력

[스케줄러] AlarmScheduler (ScheduledExecutorService)
   └── 예약 시작 시간 도달 시 "바로 사용가능합니다" 알림 자동 발송
```

**알림 발생 시점:**
- 예약 승인 / 반려
- 예약 취소
- 예약 시작 시간 (자동 스케줄)
- 비품 반납 승인

---

## DB 주요 테이블

| 테이블 | 설명 |
|--------|------|
| `user` | 사용자 정보 (is_active, user_status, permission) |
| `school` | 학교 정보 |
| `facility` | 시설 정보 |
| `equipment` | 비품 정보 |
| `equipment_detail` | 비품 낱개별 상태 |
| `reservation` | 예약 정보 (PENDING / APPROVED / REJECTED / CANCELLED) |
| `return_request` | 반납 신청 |
| `alarm` | 알림 내역 |
| `period` | 교시 정보 (시작시간 ~ 종료시간) |
| `block_period` | 제한 기간 |
| `block_schedule` | 제한 스케줄 |

---

## 테스트 계정 (더미데이터)

| 아이디 | 권한 | 이름 |
|--------|------|------|
| `admin` | ADMIN | 관리자 |
| `solmin614` | MIDDLEADMIN | 솔민 |
| `suji` | MIDDLEADMIN | 수지 |
| `minjoong` | MIDDLEADMIN | 민중 |
| (일반 사용자) | USER | 각 학년/반별 등록 |

> 초기 비밀번호는 `init.sql` 참고

---

## 팀원

| 이름 | 역할 |
|------|------|
| 김두림 (팀장) | 예약 시스템, 알림 소켓, 전체 아키텍처 |
| 솔민 | 사용자 관리, 시설/비품 관리, DB 설계 |
| 수지 | 비품 반납, 마이페이지, 제한 기간 관리 |
| 민중 | 예약 승인/반려, 제한 스케줄, 더미데이터 |

---

## 개발 환경

- Java 17
- MySQL 8.x (Docker)
- IntelliJ IDEA
- Windows 11

## test
- 2026-04-13 periodId = 1 user = cancel
- 2026-05-13 periodId = 1 user = cancel
