-- V1__create_users_and_auth.sql

CREATE TABLE admins(
    id bigint AUTO_INCREMENT primary key,
    full_name varchar(150) not null,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    role varchar(20) not null default 'ADMIN',
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp
);

CREATE TABLE patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    phone varchar(20),
    date_of_birth DATE,
    gender varchar(10),
    role varchar(20) not null default 'PATIENT',
    is_active BOOLEAN not null default true,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
);

CREATE TABLE access_logs(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_type varchar(20) not null,
    action varchar(100) not null,
    resource_type varchar(50),
    resource_id BIGINT,
    ip_address varchar(45),
    created_at timestamp with time zone not null default current_timestamp
);

CREATE INDEX idx_access_logs_user on access_logs(user_id, user_type);
CREATE INDEX idx_access_logs_resource on access_logs(resource_type, resource_id);