# SSO OAuth2 Authentication Server

This project provides an SSO authentication server that conforms to the OAuth2 specification.

# Database Setup

First, create the database in Postgres.

`CREATE DATABASE oauth2_server;`

Then, go to the `schema.sql` file. First, execute the first two lines to create the `dev` and `prod` schemas. Then run the rest of the script twice, each time with a different one of the `SET SEARCH_PATH` lines uncommented.

Lastly, run the `data.sql` file. This populates data in the `dev` schema only, to help with testing. Prod data will need to be written manually.