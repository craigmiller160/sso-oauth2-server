CREATE SCHEMA auth_server;

CREATE TABLE auth_server.users (
    id BIGSERIAL NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE auth_server.clients (
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

CREATE TABLE auth_server.roles (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    client_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (client_id) REFERENCES auth_server.clients (id)
);

CREATE TABLE auth_server.client_user_roles (
    id BIGSERIAL NOT NULL,
    client_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES auth_server.users (id),
    FOREIGN KEY (role_id) REFERENCES auth_server.roles (id),
    FOREIGN KEY (client_id) REFERENCES auth_server.clients (id),
    UNIQUE (client_id, user_id, role_id)
);

CREATE TABLE auth_server.client_users (
    id BIGSERIAL NOT NULL,
    user_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES auth_server.users (id),
    FOREIGN KEY (client_id) REFERENCES auth_server.clients (id)
);

CREATE TABLE auth_server.refresh_tokens (
    id BIGSERIAL NOT NULL,
    refresh_token TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT current_timestamp,
    PRIMARY KEY (id)
);

CREATE FUNCTION validate_client_user_role()
    RETURNS trigger AS
    $BODY$

    DECLARE role_has_client INT;
    DECLARE user_has_client INT;

    BEGIN
--         IF NEW.client_id IS NULL THEN
--             RAISE EXCEPTION 'client_id cannot be null';
--         END IF;
--
--         IF NEW.user_id IS NULL THEN
--             RAISE EXCEPTION 'user_id cannot be null';
--         END IF;
--
--         IF NEW.role_id IS NULL THEN
--             RAISE EXCEPTION 'role_id cannot be null';
--         END IF;



        SELECT COUNT(*)
        INTO role_has_client
        FROM auth_server.roles
        WHERE id = NEW.role_id
        AND client_id = NEW.client_id;

        SELECT COUNT(*)
        INTO user_has_client
        FROM auth_server.client_users
        WHERE user_id = NEW.user_id
        AND client_id = NEW.client_id;

        IF role_has_client = 0 THEN
            RAISE EXCEPTION 'Role is not allowed by client';
        END IF;

        IF user_has_client = 0 THEN
            RAISE EXCEPTION 'User is not allowed by client';
        END IF;

        RETURN NEW;
    END
    $BODY$;