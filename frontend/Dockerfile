FROM node:16.13.2-alpine AS build
WORKDIR /usr/src/app/

COPY package*.json .
RUN npm i

COPY ./ frontend/
RUN npm run build -w frontend

FROM nginx:1.17.1-alpine AS static
COPY ./nginx.conf /etc/nginx/nginx.conf
COPY --from=build /usr/src/app/frontend/dist /usr/share/nginx/html