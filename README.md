# WGU Open Skills Management Toolset (OSMT)

## Overview
The Open Skills Management Tool (OSMT, pronounced "Oz-mit") is a free, open-source instrument to facilitate the production of rich skill descriptor (RSD) based open skills libraries. In short, it helps to create a commons skills language by creating, managing, and organizing skills related data.  An open-source framework allows everyone to use the tool collaboratively to define the RSD so that those skills are translatable and transferable across educational institutions and hiring organizations within programs, curricula, and job descriptions.

## Architecture
OSMT is written with Kotlin and Angular, using backend-instances of MySQL, Redis, and Elastisearch. 
![OSMT architectural overview](./ui/src/assets/Architectural-Diagram.png)

### Dependencies
OSMT uses Elasticsearch, Redis, and MySQL as back-end dependencies. These are deployed as services via docker-compose. See additional notes below for [Configuration](#configuration).

## Build
### Requirements
OSMT requires Java 11 and a modern version of NodeJS / npm (currently v16.6.2 / 7.20.3, but any recent version should work OK). To follow the project documentation, you will need a recent version of Docker and docker-compose. 

    project root/
    |-- api                - Spring Boot, Kotlin based backend - See `./api/README.md`
    |-- ui                 - Angular frontend - See `./ui/README.md`
    |-- docker             - Misc. Docker support for development

OSMT is a multi-module Maven project, and you will also need a modern version of Maven (currently 3.8.1, but any recent version will probably work). pom.xml files exist in the project root, ./api and ./ui. Running the command `mvn clean package` from the project root will create a fat jar in the target directory that contains both the backend server and the built Angular frontend static files.

The [API](./api/README.md) and [UI](./ui/README.md) modules have their own README.md files with more specific information about those layers.

### Running Locally / Run from IntelliJ
* Import Maven project
    * Navigate to File -> New from existing sources
    * Select "Create from existing sources"
    * Select the project root folder
* Use the readme files of [`./api`](./api/README.md) and [`./ui`](./ui/README.md) to configure the submodules in IntelliJ, and to create run/debug configurations.
* Start the docker development stack for MySQL and other dependencies
    * run `docker-compose -f ./docker/dev-stack.yml up`
    * to shut down the development stack, press `<ctl> + c`
* Run both the frontend (Angular ng serve) and backend (Spring Boot) configurations you created from IntelliJ
* Visit `http://localhost:4200`

The Angular UI app is configured to proxy requests to the backend server during development. This allows one to use Angular's live reloading server.

### Configuration
OSMT can be built and deployed in a non-production context using docker-compose with [./docker-compose.yml](./docker-compose.yml) in the project root. This file builds a Docker image with Java 11 and Maven, builds the UI and API modules as a fat jar, and then stands up an application stack with the back end dependencies and a Spring application using the fat jar. This configuration should inform a production deployment, but should not be used for a production deployment.

When starting OSMT via docker-compose, you will need to provide values for OAuth2. See usage of an `osmt.env` file with docker-compose, immediately below.

### Okta Configuration
OSMT will require an OAuth2 provider. It is preconfigured for Okta, but you can use any provider. To use Okta as your OAuth2 provider, include `oauth2-okta` in the list of Spring Boot profiles. You will need to provide the following properties when running the application:
* okta.oauth2.clientId
* okta.oauth2.clientSecret
* okta.oauth2.audience
* okta.oauth2.issuer
  To get these properties, you will need a free developer account with [Okta](https://okta.com). Create an Okta web application, using the OpenID option. Navigate to Applications. In the main content pane, select the application you created. You will find the Client ID and Client Secret.
* Add a redirect URL back to http://localhost. The port will vary.
* Click the "Sign On" tab. You will find the issuer and audience values there.
  * You may find it helpful to create 2 Okta accounts for developing OSMT.
    * One account for active local development with a redirect URL that points back to http://localhost:4200 for Angular's ng serve
    * A second account for using the full non-prod docker-compose stack, with a redirect URL that points to http://localhost:8080.

You should never push these OAuth2 values to GitHub, so you should never save them in a properties file. The OSMT project is configured to git ignore files named `osmt*.env`. You can use files name with this pattern to store your OAuth2 secrets locally and pass them to a docker-compose stack, e.g., `docker-compose --env-file /path/to/osmt-angular.env -f /path/to/compose-file/yml up`.

This is the format of an env file:
```OAUTH_ISSUER=https://abcdefg.okta.com
OAUTH_CLIENTID=123456qwerty
OAUTH_CLIENTSECRET=2354asdf
OAUTH_AUDIENCE=3456zxcv
```

Also, you can provide these OAuth2 values as program arguments when starting your Spring Boot app (`-Dokta.oauth2.clientId="123456qwerty"`), either via the command line or via an IntelliJ Run config.

### Post-installation (BLS, O*NET, etc)
See the section for [Importing Skills, BLS, and O*NET](./api/README.md#importing-skills-bls-and-onet) in the API README file.

## How to get help
This project includes [./api/HELP.md](./api/HELP.md), with links to relevant references and tutorials.

OMST is an open source project, and is supported by its community. Please look to the Discussion boards and Wiki on GitHub. Please all see the [CONTRIBUTING.md](./CONTRIBUTING.md) document for additional context.


-------------------------------------------------

## Other notes
The backend will serve any routes not already configured to the API to the frontend, allowing Angular's routing to takeover.

## Deploy steps
### Create the Database and Database user
1. Copy the [./docker/mysql-init/1init.sql](docker/mysql-init/1init.sql) file from this repository to a server with access to the mysql database.
1. Run the .sql file against the mysql database: `mysql -u <DB_USER> -p -h <DB_HOST> -P 3306 < 1init.sql`

### Run The API Server from the Container
When you start up the 'apiserver' environment profile, it will automatically migrate the database to be in line with the latest schema.
Run the docker container and pass the following environment variables to it: 
 * ENVIRONMENT
 * BASE_DOMAIN
 * REDIS_URI
 * MYSQL_DB_URI
 * ELASTICSEARCH_URI
 
The use of these variables can be referenced in the [docker entrypoint script](docker/bin/docker_entrypoint.sh).

Example:
  ```
    ENVIRONMENT=review,apiserver
    BASE_DOMAIN=osmt.example.com
    REDIS_URI=<HOST>:<PORT>
    MYSQL_DB_URI=<USER>:<PASSWORD>@<HOST>:<PORT>
    ELASTICSEARCH_URI=<HOST>:<PORT>
  ```

### Manual CSV import
To do a manual batch import from a CSV:
1. Run the app container: `docker run -ti --entrypoint /bin/bash -v <full_path_to_csv_folder>:/mnt concentricsky/osmt:0.5.1`
1. Run the csv import:
```
cd /mnt/
/bin/java \
  -Dspring.profiles.active=review,import \
  -Dredis.uri=<REDIS_HOST>:<REDIS_PORT> \
  -Dapp.baseDomain=<BASE_DOMAIN> \
  -Ddb.uri=<DB_USER>:<DB_PASS>@<DB_HOST>:3306 \
  -Des.uri=<HOST>:<PORT> \
  -jar /opt/osmt/bin/osmt.jar \
  --csv=/mnt/<PATH_TO_CSV>
```
