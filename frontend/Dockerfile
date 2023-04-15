FROM node:16.13.2-alpine AS build
WORKDIR /usr/src/app/

COPY package*.json .
COPY ./frontend/package.json frontend/
RUN npm ci

COPY ./frontend/ frontend/
RUN npm run build -w frontend

FROM nginx:1.17.1-alpine AS static
COPY ./frontend/nginx.conf /etc/nginx/nginx.conf
COPY --from=build /usr/src/app/frontend/dist /usr/share/nginx/html