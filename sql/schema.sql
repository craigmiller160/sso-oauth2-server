CREATE SCHEMA dev;
CREATE SCHEMA prod;

-- SET search_path TO dev;
-- SET search_path TO prod;

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
    name VARCHAR(255) UNIQUE,
    client_key VARCHAR(255) UNIQUE NOT NULL,
    client_secret VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT false,
    allow_client_credentials BOOLEAN DEFAULT false,
    allow_password BOOLEAN DEFAULT false,
    allow_auth_code BOOLEAN DEFAULT false,
    access_token_timeout_secs INT NOT NULL,
    refresh_token_timeout_secs INT NOT NULL,
    auth_code_timeout_secs INT,
    redirect_uri VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE roles (
    id BIGSERIAL NOT NULL,
    name VARCHAR(255) UNIQUE NOT NULL,
    client_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (client_id) REFERENCES clients (id)
);

CREATE TABLE client_users (
    id BIGSERIAL NOT NULL,
    user_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
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

CREATE TABLE refresh_tokens (
    id VARCHAR(255) NOT NULL,
    refresh_token TEXT NOT NULL,
    client_id BIGINT NOT NULL,
    user_id BIGINT DEFAULT NULL,
    timestamp TIMESTAMP DEFAULT current_timestamp,
    PRIMARY KEY (id),
    FOREIGN KEY (client_id) REFERENCES clients (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    UNIQUE (client_id, user_id)
);

CREATE OR REPLACE FUNCTION validate_password()
    RETURNS trigger AS
    $BODY$

    BEGIN
        PERFORM set_config('search_path', TG_TABLE_SCHEMA, true);

        IF NEW.password IS NULL THEN
            RAISE EXCEPTION 'password cannot be null';
        END IF;

        IF TRIM(NEW.password) = '' THEN
            RAISE EXCEPTION 'password cannot be blank';
        END IF;

        RETURN NEW;
    END;
    $BODY$ LANGUAGE plpgsql;

CREATE TRIGGER password_validation
    BEFORE INSERT OR UPDATE
    ON users
    FOR EACH ROW
    EXECUTE PROCEDURE validate_password();

CREATE OR REPLACE FUNCTION validate_client_user_role()
    RETURNS trigger AS
    $BODY$

    DECLARE role_has_client INT;
    DECLARE user_has_client INT;

    BEGIN
        PERFORM set_config('search_path', TG_TABLE_SCHEMA, true);

        IF NEW.client_id IS NULL THEN
            RAISE EXCEPTION 'client_id cannot be null';
        END IF;

        IF NEW.user_id IS NULL THEN
            RAISE EXCEPTION 'user_id cannot be null';
        END IF;

        IF NEW.role_id IS NULL THEN
            RAISE EXCEPTION 'role_id cannot be null';
        END IF;

        SELECT COUNT(*)
        INTO role_has_client
        FROM roles
        WHERE id = NEW.role_id
        AND client_id = NEW.client_id;

        SELECT COUNT(*)
        INTO user_has_client
        FROM client_users
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
    $BODY$ LANGUAGE plpgsql;

CREATE TRIGGER client_user_role_validation
    BEFORE INSERT OR UPDATE
    ON client_user_roles
    FOR EACH ROW
    EXECUTE PROCEDURE validate_client_user_role();
