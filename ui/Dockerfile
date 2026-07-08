FROM node:20.18-alpine AS build

LABEL Maintainer="OSMT Contributors"
ENV BASE_DIR=/opt/osmt

# Install Node Build Dependenices
RUN apk add curl wget make chromium python3 gcc g++

WORKDIR ${BASE_DIR}

COPY ./ui/package* .

RUN --mount=type=cache,target=/root/.npm npm install

COPY ./ui/ .

FROM build AS test

ENV BASE_DIR=/opt/osmt
ENV CHROME_BIN=/usr/bin/chromium-browser

# Compile and test the UI Package
RUN --mount=type=cache,target=/root/.npm npm run ci-test

FROM test AS package

# Package the UI application
RUN --mount=type=cache,target=/root/.npm npm run build-prod

FROM nginx:alpine AS deploy

COPY --from=package /opt/osmt/dist /usr/share/nginx/html

ADD ./ui/docker/nginx/osmt.conf /etc/nginx/conf.d/site.conf

EXPOSE 8080
