# git analyser

## Setup

The application consists of a web frontend in Vue and a backend server in Sprint Boot. Both parts are required in order
th get the application to properly work.
Before you start the application in production, make sure to check the allowed routes and methods in the security
configuration of the backend.

### Frontend

In order to start development on the frontend application navigate to the frontend folder and then install the
dependencies with

```bash
npm install
```

After installing the dependencies the application can be started in development mode with

```bash
npm run dev
```

or you can build it for deployment with

```bash
npm run build
```

After building the files will be located in the dist folder and can then be hosted on a webserver.

To set up the application don't forget to set the correct environment variables as described below before building the
application!

### Backend

To start the backend run the command

```bash
cd backend
mvn spring-boot:run
```

## Environment variables

Environment variables are used to customize the application to your needs.

### Frontend

- `VITE_BASE_API_URL`: The base url the frontend can use to reach the backend. This must be set for the application to
  work!

### Backend

- `FRONTEND_REDIRECT_AFTER_LOGIN_URL`: The url the user is redirected to after logging in.
- `JWT_SECRET_KEY_PLAIN`: The secret key used to sign the JWT tokens.
- `GITLAB_CLIENT_SECRET`: The client secret for the OAuth2 authentication for the respective GitLab instance.
- `GITHUB_CLIENT_SECRET`: The client secret for the OAuth2 authentication for the respective GitHub instance.

## Setup for different GitLab instance

If you want to change the application to work on your personal gitlab instance you need to change the following files:

- values in the file `security/AuthenticationConstants.java`
- method `clientRegistrationRepository` in the file `security/SecurityConfig.java`

For the moment this application only supports GitLab and GitHub as platforms to work on.
It is not intended that this application works on more than one GitLab and on one GitHub instance at the same time -
this might be a feature for the future.
Keep the REGISTRATION_IDs in the `security/AuthenticationConstants.java` file the way they are - it might not work if
you change them.

## Feature guide

See [features](./docs/FEATURES.md)
