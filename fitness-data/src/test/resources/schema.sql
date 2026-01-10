CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    total_score INT DEFAULT 0,
    total_duration INT DEFAULT 0,
    updated_at TIMESTAMP
);
