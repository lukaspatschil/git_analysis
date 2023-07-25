# git analyser

## Setup

The application consists of a web frontend in Vue and a backend server in Sprint Boot. Both parts are required in order th get the application to properly work. 

### Frontend

In order to start development on the frontend application navigate to the frontend folder and then install the dependencies with

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

To set up the application don't forget to set the correct environment variables as described below before building the application!

### Backend

TBD

## Environment variables

Environment variables are used to customize the application to your needs.

### Frontend

- `VITE_BASE_API_URL`: The base url the frontend can use to reach the backend. This must be set for the application to work!

### Backend

- TBD

## Setup for different GitLab instance

## Feature guide

See [features](./docs/FEATURES.md)
