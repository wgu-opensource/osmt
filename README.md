# WGU Open Skills Management Toolset (OSMT)

## Overview
The Open Skills Management Tool (OSMT, pronounced "Oz-mit") is a free, open-source instrument to facilitate the production of rich skill descriptor (RSD) based open skills libraries. In short it helps to create a commons skills language by creating, managing, and organizing skills related data.  An open-source framework allows everyone to use the tool collaboratively to define the RSD so that those skills are translatable and transferable across educational institutions and hiring organizations within programs, curricula, and job descriptions.

## Architecture
OSMT is written with Kotlin and Angular, using backend-instances of MySQL, Redis, and Elastisearch. 
![OSMT architectural overview](./ui/src/assets/Architectural-Diagram.png).

## Release / Branching Strategy
The OSMT project will follow the [GitFlow](https://nvie.com/posts/a-successful-git-branching-model/) model, with
* Enhancement and bug fix work done on feature branches (```feature/branch-name```)
* Feature branches merge into ```develop```, as the integration branch
* Releases are cut from ```develop``` (as ```release-branch-name```), and merged back in to both ```master``` and ```develop```

See the [CONTRIBUTING.md](./CONTRIBUTING.md) document for additional context.

### Dependencies
OSMT stands up Elasticsearch, Redis, and MySQL dependencies via a docker-compose instance. For more context, see [docker-compose.yml](./docker-compose.yml) in the project root, and [dev-stack-local.yml](./docker/dev-stack.yml) in the ./docker directory.

#### OAuth2 and Okta Configuration
See the section for [OAuth2](./api/README.md#oauth2) in the API README file.

## Build
### Requirements
OSMT requires Java 11 and a modern version of NodeJS / npm (currently v16.6.2 / 7.20.3, but any recent version should work OK). To follow the project documentation, you will need a recent version of Docker and docker-compose. 

    project root/
    |-- api                - Spring Boot, Kotlin based backend - See `./api/README.md`
    |-- ui                 - Angular frontend - See `./ui/README.md`
    |-- docker             - Misc. Docker support for development

OSMT is a multi module Maven project, and you will also need a modern version of Maven (currently 3.8.1, but any recent version will probably work). pom.xml files exist in the project root, ./api and ./ui. Running the command `mvn clean install` from the project root will create a fat jar in the target directory that contains both the backend server and the built Angular frontend static files.
Both the API and the UI modules have README.md files with more specific information about those layers.

## Install / Run from IntelliJ
* Import maven module
    * Navigate to File -> New from existing sources
    * Select "Create from existing sources"
    * Select the project root folder
* Go through the readme files of `./api` and `./ui` to setup the child modules in IntelliJ and to create run/debug configurations. The API and UI modules require
* Start the docker development stack for MySQL and other dependencies
    * from the `./docker` folder, run `docker-compose -f dev-stack.yml up`
    * to shut down the development stack, press `<ctl> + c`
* Run both the frontend and backend configurations you created from IntelliJ
* Visit `http://localhost:4200`

The Angular UI app is configured to proxy requests to the backend server during development. This allows one to use Angular's live reloading server.


### Configuration
### Running Locally

### Post-installation (BLS, O*NET, etc)
See the section for [Importing Skills, BLS, and O*NET](./api/README.md#importing-skills-bls-and-onet) in the API README file.

## How to get help
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
