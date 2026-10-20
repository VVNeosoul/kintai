CREATE DATABASE IF NOT EXISTS kintai DEFAULT CHARACTER SET utf8mb4;
USE kintai;

CREATE TABLE employees (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           employee_code VARCHAR(20) NOT NULL UNIQUE,
                           name VARCHAR(50) NOT NULL,
                           password VARCHAR(100) NOT NULL,
                           role VARCHAR(20) NOT NULL DEFAULT 'EMPLOYEE',
                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            employee_id BIGINT NOT NULL,
                            work_date DATE NOT NULL,
                            clock_in DATETIME,
                            clock_out DATETIME,
                            UNIQUE KEY uk_emp_date (employee_id, work_date),
                            FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE leave_requests (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                employee_id BIGINT NOT NULL,
                                leave_date DATE NOT NULL,
                                reason VARCHAR(200),
                                status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (employee_id) REFERENCES employees(id)
);