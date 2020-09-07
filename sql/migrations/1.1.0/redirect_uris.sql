-- DON'T FORGET TO SET search_path FIRST

ALTER TABLE clients
DROP COLUMN redirect_uri;

ALTER TABLE clients
ALTER COLUMN auth_code_timeout_secs SET NOT NULL;

CREATE TABLE client_redirect_uris (
    id BIGSERIAL NOT NULL,
    client_id BIGINT NOT NULL,
    redirect_uri VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE client_redirect_uris
ADD CONSTRAINT client_redirect_uris_client_id_fk FOREIGN KEY (client_id) REFERENCES clients (id);

ALTER TABLE client_redirect_uris
ADD CONSTRAINT client_redirect_uris_client_id_redirect_uri_unique UNIQUE (client_id, redirect_uri);
