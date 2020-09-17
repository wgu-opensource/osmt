CREATE DATABASE IF NOT EXISTS osmt_db character set UTF8mb3 collate utf8mb3_unicode_ci;

/* CHANGE TO SECURE VALUES! */
CREATE USER 'osmt_db_user'@'%' IDENTIFIED BY 'password';
GRANT ALL ON  osmt_db.* TO 'osmt_db_user'@'%';
