
ALTER TABLE clients
DROP COLUMN redirect_uri;

ALTER TABLE clients
ALTER COLUMN auth_code_timeout_secs SET NOT NULL;

CREATE TABLE client_redirect_uris (
    id BIGSERIAL NOT NULL,
    client_id BIGINT NOT NULL,
    redirect_uri VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (client_id) REFERENCES clients (id)
);
