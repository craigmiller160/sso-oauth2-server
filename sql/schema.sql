CREATE TABLE users (
    id BIGSERIAL NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE roles (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE user_roles (
    id BIGSERIAL NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE clients (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255),
    client_key VARCHAR(255) NOT NULL,
    client_secret VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT false,
    PRIMARY KEY (id)
);

CREATE TABLE client_users (
    id BIGSERIAL NOT NULL,
    user_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (client_id) REFERENCES clients (id)
);