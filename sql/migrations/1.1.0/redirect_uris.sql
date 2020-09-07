-- DON'T FORGET TO SET search_path FIRST

ALTER TABLE clients
DROP COLUMN redirect_uri;

ALTER TABLE clients
ALTER COLUMN auth_code_timeout_secs SET NOT NULL;

CREATE SEQUENCE client_redirect_uris_id_seq START 1;

CREATE TABLE client_redirect_uris (
    id BIGINT NOT NULL DEFAULT nextval('client_redirect_uris_id_seq'::regclass),
    client_id BIGINT NOT NULL,
    redirect_uri VARCHAR(255) NOT NULL,
    CONSTRAINT client_redirect_uris_id_pk PRIMARY KEY (id),
    CONSTRAINT client_redirect_uris_client_id_fk FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT client_redirect_uris_client_id_redirect_uri_unique UNIQUE (client_id, redirect_uri)
);
