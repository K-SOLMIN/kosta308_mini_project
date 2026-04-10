-- ============================================================
-- kimdoolim 프로젝트 초기화 SQL
-- DB: kimdoolim / charset: utf8mb4
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE kimdoolim;

-- ============================================================
-- 1. user
-- ============================================================
CREATE TABLE IF NOT EXISTS user (
    user_id     INT             NOT NULL AUTO_INCREMENT,
    school_id   INT             NOT NULL DEFAULT 0,
    id          VARCHAR(50)     NOT NULL UNIQUE,
    password    VARCHAR(100)    NOT NULL,
    permission  VARCHAR(20)     NOT NULL DEFAULT 'USER'
                    COMMENT 'ADMIN | MIDDLEADMIN | USER',
    name        VARCHAR(50)     NOT NULL,
    phone       VARCHAR(20)     NULL,
    grade_no    INT             NOT NULL DEFAULT 0,
    class_no    INT             NOT NULL DEFAULT 0,
    is_active   VARCHAR(10)     NOT NULL DEFAULT 'true'
                    COMMENT 'true | false',
    user_status VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                    COMMENT 'ACTIVE | 휴직 | 전근',
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. period (교시)
-- ============================================================
CREATE TABLE IF NOT EXISTS period (
    period_id   INT         NOT NULL AUTO_INCREMENT,
    period_name VARCHAR(30) NOT NULL,
    start_time  TIME        NOT NULL,
    end_time    TIME        NOT NULL,
    PRIMARY KEY (period_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. facility (시설)
-- ============================================================
CREATE TABLE IF NOT EXISTS facility (
    facility_id             BIGINT      NOT NULL AUTO_INCREMENT,
    manager_id              INT         NULL
                                COMMENT 'FK -> user.user_id (담당 중간관리자)',
    location                VARCHAR(100) NOT NULL,
    name                    VARCHAR(100) NOT NULL,
    max_capacity            INT         NOT NULL DEFAULT 0,
    max_reservation_unit    VARCHAR(20) NULL
                                COMMENT '예: 일 | 주',
    max_reservation_value   INT         NOT NULL DEFAULT 0,
    is_delete               VARCHAR(10) NOT NULL DEFAULT 'false'
                                COMMENT 'true | false',
    deletedate              DATETIME    NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT '정상'
                                COMMENT '정상 | 수리 | 점검',
    PRIMARY KEY (facility_id),
    CONSTRAINT fk_facility_manager FOREIGN KEY (manager_id)
        REFERENCES user (user_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. equipment (비품 세트)
-- ============================================================
CREATE TABLE IF NOT EXISTS equipment (
    equipment_id    BIGINT      NOT NULL AUTO_INCREMENT,
    manager_id      INT         NULL
                        COMMENT 'FK -> user.user_id (담당 중간관리자)',
    name            VARCHAR(100) NOT NULL,
    location        VARCHAR(100) NOT NULL,
    check_delete    VARCHAR(10) NOT NULL DEFAULT 'false'
                        COMMENT 'true | false',
    deletedate      DATE        NULL,
    serial_no       VARCHAR(100) NULL
                        COMMENT '기본 시리얼 or 자동생성 접두사',
    status          VARCHAR(20) NOT NULL DEFAULT '정상'
                        COMMENT '정상 | 수리 | 점검',
    PRIMARY KEY (equipment_id),
    CONSTRAINT fk_equipment_manager FOREIGN KEY (manager_id)
        REFERENCES user (user_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. equipmentdetail (비품 낱개)
-- ============================================================
CREATE TABLE IF NOT EXISTS equipmentdetail (
    equipment_detail_id BIGINT      NOT NULL AUTO_INCREMENT,
    equipment_id        BIGINT      NOT NULL,
    check_delete        VARCHAR(10) NOT NULL DEFAULT 'false'
                            COMMENT 'true | false',
    delete_date         DATE        NULL,
    serial_no           VARCHAR(100) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT '정상'
                            COMMENT '정상 | 수리 | 점검 | 분실',
    PRIMARY KEY (equipment_detail_id),
    CONSTRAINT fk_equipdetail_equip FOREIGN KEY (equipment_id)
        REFERENCES equipment (equipment_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6. reservation (예약)
-- ============================================================
CREATE TABLE IF NOT EXISTS reservation (
    reservation_id  BIGINT          NOT NULL AUTO_INCREMENT,
    period_id       INT             NOT NULL,
    user_id         INT             NOT NULL,
    facility_id     BIGINT          NULL,
    equipment_id    BIGINT          NULL,
    purpose         VARCHAR(200)    NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reservation_date DATE           NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT '대기'
                        COMMENT '대기 | 승인 | 거절 | 취소 | 반납완료',
    reason          VARCHAR(100)    NULL
                        COMMENT '거절 또는 취소 시 사유',
    real_use        VARCHAR(10)     NOT NULL DEFAULT 'false'
                        COMMENT 'true | false',
    target_type     VARCHAR(20)     NOT NULL
                        COMMENT 'FACILITY | EQUIPMENT',
    approved_at     DATETIME        NULL,
    PRIMARY KEY (reservation_id),
    CONSTRAINT chk_reason_required CHECK (
        status NOT IN ('거절', '취소', '반려', '강제취소')
        OR (reason IS NOT NULL AND reason <> '')
    ),
    CONSTRAINT fk_reservation_period    FOREIGN KEY (period_id)
        REFERENCES period (period_id),
    CONSTRAINT fk_reservation_user      FOREIGN KEY (user_id)
        REFERENCES user (user_id),
    CONSTRAINT fk_reservation_facility  FOREIGN KEY (facility_id)
        REFERENCES facility (facility_id) ON DELETE SET NULL,
    CONSTRAINT fk_reservation_equipment FOREIGN KEY (equipment_id)
        REFERENCES equipment (equipment_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 7. return_request (반납 요청)
-- ============================================================
CREATE TABLE IF NOT EXISTS return_request (
    return_request_id   BIGINT      NOT NULL AUTO_INCREMENT,
    reservation_id      BIGINT      NOT NULL,
    `condition`         VARCHAR(200) NOT NULL DEFAULT '정상'
                            COMMENT '반납 물품 상태 메모',
    status              VARCHAR(20) NOT NULL DEFAULT '반납완료',
    created_at          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (return_request_id),
    CONSTRAINT fk_returnreq_reservation FOREIGN KEY (reservation_id)
        REFERENCES reservation (reservation_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 8. block_schedule (교시별 차단 일정)
--    block_date 있으면 특정 날짜, repeat_day_of_week 있으면 반복 요일
-- ============================================================
CREATE TABLE IF NOT EXISTS block_schedule (
    block_schedule_id   BIGINT      NOT NULL AUTO_INCREMENT,
    block_date          DATE        NULL
                            COMMENT '특정 날짜 차단 시 사용',
    period_id           INT         NOT NULL,
    repeat_day_of_week  INT         NULL
                            COMMENT 'DAYOFWEEK() 기준: 1=일 ~ 7=토',
    repeat_start_date   DATE        NULL,
    repeat_end_date     DATE        NULL,
    description         VARCHAR(200) NOT NULL,
    facility_id         BIGINT      NULL
                            COMMENT 'NULL이면 전체 시설 적용',
    equipment_id        BIGINT      NULL
                            COMMENT 'NULL이면 전체 비품 적용',
    PRIMARY KEY (block_schedule_id),
    CONSTRAINT fk_blocksch_period    FOREIGN KEY (period_id)
        REFERENCES period (period_id),
    CONSTRAINT fk_blocksch_facility  FOREIGN KEY (facility_id)
        REFERENCES facility (facility_id) ON DELETE CASCADE,
    CONSTRAINT fk_blocksch_equipment FOREIGN KEY (equipment_id)
        REFERENCES equipment (equipment_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 9. block_period (기간 차단 마스터)
--    period_id NULL이면 종일 차단
-- ============================================================
CREATE TABLE IF NOT EXISTS block_period (
    block_period_id             BIGINT      NOT NULL AUTO_INCREMENT,
    block_period_startdate      DATE        NOT NULL,
    block_period_enddate        DATE        NOT NULL,
    block_period_description    VARCHAR(200) NOT NULL,
    period_id                   INT         NULL
                                    COMMENT 'NULL이면 종일 차단',
    PRIMARY KEY (block_period_id),
    CONSTRAINT fk_blockperiod_period FOREIGN KEY (period_id)
        REFERENCES period (period_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 10. block_period_detail (기간 차단 대상 상세)
-- ============================================================
CREATE TABLE IF NOT EXISTS block_period_detail (
    block_period_detail_id  BIGINT  NOT NULL AUTO_INCREMENT,
    block_period_id         BIGINT  NOT NULL,
    facility_id             BIGINT  NULL,
    equipment_id            BIGINT  NULL,
    PRIMARY KEY (block_period_detail_id),
    CONSTRAINT fk_bpdetail_master    FOREIGN KEY (block_period_id)
        REFERENCES block_period (block_period_id) ON DELETE CASCADE,
    CONSTRAINT fk_bpdetail_facility  FOREIGN KEY (facility_id)
        REFERENCES facility (facility_id) ON DELETE CASCADE,
    CONSTRAINT fk_bpdetail_equipment FOREIGN KEY (equipment_id)
        REFERENCES equipment (equipment_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 11. alarm (알림)
-- ============================================================
CREATE TABLE IF NOT EXISTS alarm (
    alarm_id        INT             NOT NULL AUTO_INCREMENT,
    receiver_id     INT             NOT NULL,
    type            VARCHAR(10)     NOT NULL
                        COMMENT 'START | RETURN 등',
    generate_date   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    content         VARCHAR(100)    NOT NULL,
    isread          VARCHAR(10)     NOT NULL DEFAULT 'false'
                        COMMENT 'false | TRUE | FALSE',
    readdate        DATE            NULL,
    PRIMARY KEY (alarm_id),
    CONSTRAINT fk_alarm_receiver FOREIGN KEY (receiver_id)
        REFERENCES user (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 샘플 데이터
-- ============================================================

-- 사용자 (비밀번호 평문)
-- ADMIN
INSERT INTO user (school_id, id, password, permission, name, phone, grade_no, class_no, is_active, user_status)
VALUES (1, 'admin', '1111', 'ADMIN', '관리자', '010-0000-0000', 0, 0, 'true', 'ACTIVE');

-- MIDDLEADMIN (시설담당, 비품담당)
INSERT INTO user (school_id, id, password, permission, name, phone, grade_no, class_no, is_active, user_status)
VALUES
(1, 'manager1', '1111', 'MIDDLEADMIN', '김중간', '010-1111-1111', 0, 0, 'true', 'ACTIVE'),
(1, 'manager2', '1111', 'MIDDLEADMIN', '이중간', '010-2222-2222', 0, 0, 'true', 'ACTIVE');

-- USER (일반 교사/학생)
INSERT INTO user (school_id, id, password, permission, name, phone, grade_no, class_no, is_active, user_status)
VALUES
(1, 'suji', '1234', 'ADMIN', '임수지', '010-3333-3333', 1, 1, 'true', 'ACTIVE'),
(1, 'solmin', '1234', 'ADMIN', '김솔민', '010-4444-4444', 1, 2, 'true', 'ACTIVE'),
(1, 'minjoong', '1234', 'ADMIN', '김민중', '010-5555-5555', 2, 1, 'true', 'ACTIVE'),
(1, 'user01', '1234', 'USER', '홍길동', '010-6666-6666', 6, 3, 'true', 'ACTIVE');

-- 교시 (period)
INSERT INTO period (period_name, start_time, end_time)
VALUES
('1교시', '09:00:00', '10:00:00'),
('2교시', '10:10:00', '11:10:00'),
('3교시', '11:20:00', '12:20:00'),
('4교시', '13:20:00', '14:20:00'),
('5교시', '14:30:00', '15:30:00'),
('6교시', '15:40:00', '16:40:00');

-- 시설 (facility) — manager1(user_id=2) 담당
INSERT INTO facility (manager_id, location, name, max_capacity, max_reservation_unit, max_reservation_value, is_delete, status)
VALUES
(2, '본관 1층', '대강당',    200, '일', 3, 'false', '정상'),
(2, '본관 2층', '회의실 A',   20, '일', 2, 'false', '정상'),
(2, '별관 1층', '컴퓨터실 1', 30, '일', 1, 'false', '정상'),
(2, '별관 2층', '세미나실',   15, '일', 2, 'false', '정상');

-- 비품 세트 (equipment) — manager2(user_id=3) 담당
INSERT INTO equipment (manager_id, name, location, check_delete, serial_no, status)
VALUES
(3, '노트북', '비품실 A', 'false', 'NB', '정상'),
(3, '빔프로젝터', '비품실 A', 'false', 'BP', '정상'),
(3, '확성기', '비품실 B', 'false', 'HS', '정상');

-- 비품 낱개 (equipmentdetail)
-- 노트북 5대 (equipment_id=1)
INSERT INTO equipmentdetail (equipment_id, check_delete, serial_no, status)
VALUES
(1, 'false', 'NB-001', '정상'),
(1, 'false', 'NB-002', '정상'),
(1, 'false', 'NB-003', '정상'),
(1, 'false', 'NB-004', '정상'),
(1, 'false', 'NB-005', '정상');

-- 빔프로젝터 3대 (equipment_id=2)
INSERT INTO equipmentdetail (equipment_id, check_delete, serial_no, status)
VALUES
(2, 'false', 'BP-001', '정상'),
(2, 'false', 'BP-002', '정상'),
(2, 'false', 'BP-003', '수리');

-- 확성기 2대 (equipment_id=3)
INSERT INTO equipmentdetail (equipment_id, check_delete, serial_no, status)
VALUES
(3, 'false', 'HS-001', '정상'),
(3, 'false', 'HS-002', '정상');
