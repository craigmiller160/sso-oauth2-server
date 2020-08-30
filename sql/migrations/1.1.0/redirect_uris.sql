
ALTER TABLE clients
DROP COLUMN redirect_uri;

CREATE TABLE client_redirect_uris (
    id BIGSERIAL NOT NULL,
    client_id BIGINT NOT NULL,
    redirect_uri VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (redirect_uri) REFERENCES clients (id)
);
