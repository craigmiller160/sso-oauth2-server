CREATE SCHEMA dev;
CREATE SCHEMA prod;

-- SET search_path TO dev;
-- SET search_path TO prod;

CREATE SEQUENCE users_id_seq START 1;

CREATE TABLE users (
    id BIGINT NOT NULL DEFAULT nextval('users_id_seq'::regclass),
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT users_id_pk PRIMARY KEY (id),
    CONSTRAINT users_email_unique UNIQUE (email)
);

CREATE SEQUENCE clients_id_seq START 1;

CREATE TABLE clients (
    id BIGINT NOT NULL DEFAULT nextval('clients_id_seq'::regclass),
    name VARCHAR(255) NOT NULL,
    client_key VARCHAR(255) NOT NULL,
    client_secret VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT false,
    access_token_timeout_secs INT NOT NULL,
    refresh_token_timeout_secs INT NOT NULL,
    auth_code_timeout_secs INT,
    redirect_uri VARCHAR(255),
    CONSTRAINT clients_id_pk PRIMARY KEY (id),
    CONSTRAINT clients_name_unique UNIQUE (name),
    CONSTRAINT clients_client_key_unique UNIQUE (client_key)
);

CREATE SEQUENCE roles_id_seq START 1;

CREATE TABLE roles (
    id BIGINT NOT NULL DEFAULT nextval('roles_id_seq'::regclass),
    name VARCHAR(255) NOT NULL,
    client_id BIGINT NOT NULL,
    CONSTRAINT roles_id_pk PRIMARY KEY (id),
    CONSTRAINT roles_client_id_fk FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT roles_name_client_id_unique UNIQUE (name, client_id)
);

CREATE SEQUENCE client_users_id_seq START 1;

CREATE TABLE client_users (
    id BIGINT NOT NULL DEFAULT nextval('client_users_id_seq'::regclass),
    user_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    CONSTRAINT client_users_id_pk PRIMARY KEY (id),
    CONSTRAINT client_users_user_id_fk FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT client_users_client_id_fk FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT client_users_user_id_client_id_unique UNIQUE (user_id, client_id)
);

CREATE SEQUENCE client_user_roles_id_seq START 1;

CREATE TABLE client_user_roles (
    id BIGINT NOT NULL DEFAULT nextval('client_user_roles'::regclass),
    client_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT client_user_roles_id_pk PRIMARY KEY (id),
    CONSTRAINT client_user_roles_user_id_fk FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT client_user_roles_client_id_fk FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT client_user_roles_role_id_fk FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT client_user_roles_client_id_user_id_role_id_unique UNIQUE (client_id, user_id, role_id)
);

CREATE TABLE refresh_tokens (
    id VARCHAR(255) NOT NULL,
    refresh_token TEXT NOT NULL,
    client_id BIGINT NOT NULL,
    user_id BIGINT DEFAULT NULL,
    timestamp TIMESTAMP DEFAULT current_timestamp,
    CONSTRAINT refresh_tokens_id_pk PRIMARY KEY (id),
    CONSTRAINT refresh_tokens_client_id_fk FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT refresh_tokens_user_id_fk FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT refresh_tokens_client_id_user_id_unique UNIQUE (client_id, user_id)
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
