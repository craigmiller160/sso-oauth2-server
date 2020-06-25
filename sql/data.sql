INSERT INTO dev.users (id, email, first_name, last_name, password)
VALUES (1, 'craig@gmail.com', 'Craig', 'Miller', '{bcrypt}$2a$10$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe'),
       (2, 'bob@gmail.com', 'Bob', 'Saget', '{bcrypt}$2a$10$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe');

INSERT INTO dev.clients (id, name, client_key, client_secret, enabled, allow_client_credentials, allow_password, allow_auth_code, access_token_timeout_secs, refresh_token_timeout_secs)
VALUES (1, 'ClientCredsApp', 'client_creds', '{bcrypt}$2a$10$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe', true, true, false, false, 300, 3600),
       (2, 'PasswordApp', 'password', '{bcrypt}$2a$10$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe', true, false, true, false, 300, 3600),
       (3, 'AuthCodeApp', 'auth_code', '{bcrypt}$2a$10$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe', true, false, false, true, 300, 3600);

INSERT INTO dev.roles (id, name, client_id)
VALUES (1, 'ROLE_READ', 2),
       (2, 'ROLE_WRITE', 2);

INSERT INTO dev.client_users (id, user_id, client_id)
VALUES (1, 1, 2);

INSERT INTO dev.client_user_roles (user_id, client_id, role_id)
VALUES (1, 2, 1), (1, 2, 2);