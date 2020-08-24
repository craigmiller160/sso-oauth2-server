# SSO OAuth2 Authentication Server

This project provides an SSO authentication server that conforms to the OAuth2 specification.

## Database Setup

First, create the database in Postgres.

`CREATE DATABASE oauth2_server;`

Then, go to the `schema.sql` file. First, execute the first two lines to create the `dev` and `prod` schemas. Then run the rest of the script twice, each time with a different one of the `SET SEARCH_PATH` lines uncommented.

Lastly, run the `data.sql` file. This populates data in the `dev` schema only, to help with testing. Prod data will need to be written manually.

## Running Locally

To run the app locally, the provided run script must be used.

```
sh run.sh
```

## Supported Authentications

This application supports OAuth2 `client_credentials`, `password`, `refresh_token`, and `auth_code` grant types.

### client_credentials

```
    POST /oauth/token
    Authorization: Basic #######
    grant_type=client_credentials
```

The `client_credentials` grant type is the simplest one. It allows a client (generally a single application) to authenticate itself. There is no fine-grained access roles at all in this, just a simple yes/no authentication check.

The client key/secret are provided via Basic Auth. Otherwise, just the grant type is set in the urlencoded body.

NOTE: client_credentials support has been disabled for the time being and cannot be used.

### password

```
    POST /oauth/token
    Authorization: Basic ######
    grant_type=password&username=user&password=password
```

The `password` grant type is a simple user authentication flow. It is useful for multi-user applications where different users have different permissions.

The client key/secret are provided via Basic Auth. Otherwise, the grant type is set to "password", and the username/password must be included in the urlencoded body.

### refresh_token

```
    POST /oauth/token
    Authorization: Basic #####
    grant_type=refresh_token&refresh_token=######
``` 

The `refresh_token` grant type is used to refresh a token. All authentication flows provide refresh tokens, which will be used after the access token expires.

The client key/secret are provided via Basic Auth. Otherwise, the grant type is set to "refresh_token", and the refresh token itself must be included in the urlencoded body.
