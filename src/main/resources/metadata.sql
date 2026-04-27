CREATE TABLE file_metadata (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) UNIQUE,
    status VARCHAR(50),
    record_count INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);