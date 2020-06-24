INSERT INTO auth_server.users (email, first_name, last_name, password)
VALUES ('craig@gmail.com', 'Craig', 'Miller', '{bcrypt}$2a$10$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe'),
       ('bob@gmail.com', 'Bob', 'Saget', '{bcrypt}$2a$10$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe');

INSERT INTO auth_server.clients (name, client_key, client_secret, enabled, allow_client_credentials, allow_password, allow_auth_code)
VALUES ('ClientCredsApp', 'client_creds', '{bcrypt}$2a$10$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe', true, true, false, false),
       ('PasswordApp', 'password', '{bcrypt}$2a$10$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe', true, false, true, false),
       ('AuthCodeApp', 'auth_code', '{bcrypt}$2a$10$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe', true, false, false, true);

