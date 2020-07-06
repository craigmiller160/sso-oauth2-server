INSERT INTO dev.users (id, email, first_name, last_name, password)
VALUES (1, 'craig@gmail.com', 'Craig', 'Miller', '{bcrypt}$2a$10$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe'),
       (2, 'bob@gmail.com', 'Bob', 'Saget', '{bcrypt}$2a$10$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe'),
       (3, 'auth@gmail.com', 'Auth', 'Code', '{bcrypt}$2a$10$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe');

INSERT INTO dev.clients (id, name, client_key, client_secret, enabled, allow_client_credentials, allow_password, allow_auth_code, access_token_timeout_secs, refresh_token_timeout_secs, auth_code_timeout_secs, redirect_uri)
VALUES (1, 'ClientCredsApp', 'client_creds', '{bcrypt}$2a$10$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe', true, true, false, false, 300, 3600, null, null),
       (2, 'PasswordApp', 'password', '{bcrypt}$2a$10$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe', true, false, true, false, 300, 3600, null, null),
       (3, 'AuthCodeApp', 'auth_code', '{bcrypt}$2a$10$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe', true, false, false, true, 300, 3600, 60, 'http://somewhere.com'),
       (4, 'auth-management-service', 'a4cc4fef-564e-44c1-82af-45572f124c1a', '{bcrypt}$2a$10$Mo7pB5wHzuChfanS1c9vOOKRmdn0.TEWDi43yjd6jstdkHtmT/FXa', true, false, true, true, 300, 3600, 60, 'https://localhost:7004/api/authcode/code');

INSERT INTO dev.roles (id, name, client_id)
VALUES (1, 'ROLE_READ', 2),
       (2, 'ROLE_WRITE', 2);

INSERT INTO dev.client_users (id, user_id, client_id)
VALUES (1, 1, 2), (2, 3, 3), (3, 1, 4);

INSERT INTO dev.client_user_roles (user_id, client_id, role_id)
VALUES (1, 2, 1), (1, 2, 2);
