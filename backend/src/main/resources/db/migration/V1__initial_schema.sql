-- Worktime Tracking System - Final Schema
-- MariaDB 10.6+ / MySQL 8.0.16+ compatible
--
-- Design decisions:
-- 1) All timestamps are provided by the application in UTC (no CURRENT_TIMESTAMP defaults).
-- 2) created_at is immutable (enforced via BEFORE UPDATE triggers).
-- 3) “Only one open session” and “only one open segment per session” enforced via generated open_flag + UNIQUE.
--
-- Optional (recommended hygiene): keep server/session in UTC (safe even if app supplies all timestamps)
SET time_zone = '+00:00';

-- ----------------------------
-- Table: category
-- ----------------------------
CREATE TABLE IF NOT EXISTS category (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(255) NOT NULL,
    active       BOOLEAN NOT NULL DEFAULT TRUE,
    description  VARCHAR(500),

    -- Auditing (set by application, UTC)
    created_at   DATETIME(3) NOT NULL,
    updated_at   DATETIME(3) NOT NULL,

    CONSTRAINT uq_category_name UNIQUE (name),
    CONSTRAINT chk_category_name_not_blank CHECK (CHAR_LENGTH(TRIM(name)) > 0),

    KEY idx_category_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table: activity
-- ----------------------------
CREATE TABLE IF NOT EXISTS activity (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(255) NOT NULL,
    active       BOOLEAN NOT NULL DEFAULT TRUE,
    description  VARCHAR(500),

    -- Auditing (set by application, UTC)
    created_at   DATETIME(3) NOT NULL,
    updated_at   DATETIME(3) NOT NULL,

    CONSTRAINT uq_activity_name UNIQUE (name),
    CONSTRAINT chk_activity_name_not_blank CHECK (CHAR_LENGTH(TRIM(name)) > 0),

    KEY idx_activity_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table: work_session (day/shift container)
-- ----------------------------
CREATE TABLE IF NOT EXISTS work_session (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- Session interval (set by application, UTC)
    start_time   DATETIME(3) NOT NULL,
    end_time     DATETIME(3) NULL DEFAULT NULL,

    -- Generated Helper for day-grouping
    start_date   DATE GENERATED ALWAYS AS (DATE(start_time)) VIRTUAL,
    -- Generated flag to enforce "only one open work_session"
    open_flag    TINYINT(1) GENERATED ALWAYS AS (IF(end_time IS NULL, 1, NULL)) VIRTUAL,

    -- Auditing (set by application, UTC)
    created_at   DATETIME(3) NOT NULL,
    updated_at   DATETIME(3) NOT NULL,

    CONSTRAINT chk_work_session_range CHECK (end_time IS NULL OR end_time >= start_time),

    UNIQUE KEY uq_work_session_one_open (open_flag),
    KEY idx_work_session_start (start_time),
    KEY idx_work_session_end (end_time),
    KEY idx_work_session_start_date (start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table: work_segment (splits a session across activities)
-- ----------------------------
CREATE TABLE IF NOT EXISTS work_segment (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_session_id BIGINT NOT NULL,
    category_id     BIGINT NOT NULL,
    activity_id     BIGINT NOT NULL,
    -- Session interval (set by application, UTC)
    start_time      DATETIME(3) NOT NULL,
    end_time        DATETIME(3) NULL DEFAULT NULL,
    comment         VARCHAR(500),

    -- Generated Helper for day-grouping
    start_date      DATE GENERATED ALWAYS AS (DATE(start_time)) VIRTUAL,
    -- Generated flag to enforce "only one open work_session"
    open_flag       TINYINT(1) GENERATED ALWAYS AS (IF(end_time IS NULL, 1, NULL)) VIRTUAL,

    -- Auditing (set by application, UTC)
    created_at      DATETIME(3) NOT NULL,
    updated_at      DATETIME(3) NOT NULL,

    CONSTRAINT fk_work_segment_work_session
        FOREIGN KEY (work_session_id) REFERENCES work_session(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_work_segment_activity
        FOREIGN KEY (activity_id) REFERENCES activity(id)
        ON DELETE RESTRICT,

    -- Prevent deleting categories referenced by history
    CONSTRAINT fk_work_segment_category
        FOREIGN KEY (category_id) REFERENCES category(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_work_segment_range CHECK (end_time IS NULL OR end_time >= start_time),

    UNIQUE KEY uq_work_segment_one_open_per_session (work_session_id, open_flag),

    KEY idx_work_segment_start_date (start_date),

    -- Reporting-friendly indexes
    KEY idx_work_segment_category_start (category_id, start_time),
    KEY idx_work_segment_activity_start (activity_id, start_time),
    KEY idx_work_segment_category_activity_start (category_id, activity_id, start_time),

    KEY idx_work_segment_session_start (work_session_id, start_time),
    KEY idx_work_segment_session_end (work_session_id, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Triggers: enforce created_at immutability
-- ----------------------------
DELIMITER //

DROP TRIGGER IF EXISTS trg_category_created_at_immutable//
CREATE TRIGGER trg_category_created_at_immutable
BEFORE UPDATE ON category
FOR EACH ROW
BEGIN
  SET NEW.created_at = OLD.created_at;
END//

DROP TRIGGER IF EXISTS trg_activity_created_at_immutable//
CREATE TRIGGER trg_activity_created_at_immutable
BEFORE UPDATE ON activity
FOR EACH ROW
BEGIN
  SET NEW.created_at = OLD.created_at;
END//

DROP TRIGGER IF EXISTS trg_work_session_created_at_immutable//
CREATE TRIGGER trg_work_session_created_at_immutable
BEFORE UPDATE ON work_session
FOR EACH ROW
BEGIN
  SET NEW.created_at = OLD.created_at;
END//

DROP TRIGGER IF EXISTS trg_work_segment_created_at_immutable//
CREATE TRIGGER trg_work_segment_created_at_immutable
BEFORE UPDATE ON work_segment
FOR EACH ROW
BEGIN
  SET NEW.created_at = OLD.created_at;
END//

DELIMITER ;
