CREATE TABLE users (
    id BIGSERIAL NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE clients (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255),
    client_key VARCHAR(255) UNIQUE NOT NULL,
    client_secret VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT false,
    allow_client_credentials BOOLEAN DEFAULT false,
    allow_password BOOLEAN DEFAULT false,
    allow_auth_code BOOLEAN DEFAULT false,
    PRIMARY KEY (id)
);

CREATE TABLE roles (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    client_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (client_id) REFERENCES clients (id)
);

CREATE TABLE client_user_roles (
    id BIGSERIAL NOT NULL,
    client_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (role_id) REFERENCES roles (id),
    FOREIGN KEY (client_id) REFERENCES clients (id),
    UNIQUE (client_id, user_id, role_id)
);

CREATE TABLE client_users (
    id BIGSERIAL NOT NULL,
    user_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (client_id) REFERENCES clients (id)
);

CREATE TABLE refresh_tokens (
    id BIGSERIAL NOT NULL,
    refresh_token TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT current_timestamp,
    PRIMARY KEY (id)
);