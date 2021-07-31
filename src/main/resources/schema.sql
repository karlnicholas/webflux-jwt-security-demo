DROP TABLE IF EXISTS users;
CREATE TABLE users
(
    username   VARCHAR(64) PRIMARY KEY,
    password   VARCHAR(64),
    roles      ARRAY,
    first_name VARCHAR(64),
    last_name  VARCHAR(64),
    enabled    BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
