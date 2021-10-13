# WGU Open Skills Management Toolset (OSMT)

## Overview
The Open Skills Management Tool (OSMT, pronounced "oz-mit") is a free, open-source instrument to facilitate the production of Rich Skill Descriptor (RSD) based open skills libraries. In short, it helps to create a commons skills language by creating, managing, and organizing skills-related data. An open-source framework allows everyone to use the tool collaboratively to define the RSD, so that those skills are translatable and transferable across educational institutions and hiring organizations within programs, curricula, and job descriptions.

## Architecture
OSMT is written in Kotlin with Spring Boot, and Angular. It uses backend-instances of MySQL, Redis, and Elastisearch.
![OSMT architectural overview](./ui/src/assets/Architectural-Diagram.png)

### Dependencies
OSMT uses Elasticsearch, Redis, and MySQL as back-end dependencies. Any OSMT instance beyond a local development server also requires an OAuth2 provider. See additional notes below for [Configuration](#configuration).

### Requirements to Build OSMT
OSMT requires certain software and SDKs to build:
* a Java 11 JDK (OpenJDK works fine)
* Maven 3.8.1 or higher
* NodeJS v10.16.0 / npm 6.10.2 or higher
   * NodeJS/npm are used for building client code; there are no runtime NodeJS/npm dependencies.
   * Maven uses an embedded copy of Node v10.16.0 and npm 6.10.2 (see [About frontend-maven-plugin](./ui/README.md#about-frontend-maven-plugin) in the UI README file). 
   * Locally, a developer probably has their own versions of NodeJS and npm installed. They should be >= the versions given above.
* a recent version of Docker and docker-compose

### Project Structure
OSMT is a multi-module Maven project. pom.xml files exist in the project root, `./api` and `./ui` directories. Running the command `mvn clean package` from the project root will create a fat jar in the target directory that contains both the backend server and the prod-built Angular frontend static files.

    project root/
    |-- api                - Spring Boot, Kotlin-based backend
    |-- ui                 - Angular frontend
    \-- docker             - Misc. Docker support for development

The [API](./api/README.md) and [UI](./ui/README.md) modules have their own README.md files with more specific information about those layers.

### Configuration
* This project makes use of 2 similar configurations, labelled in this documentation as "Quickstart" and "Development".
* OSMT requires an OAuth2 provider. It is preconfigured for Okta, but you can use any provider.

#### Quickstart Configuration
TODO - something other than "Quickstart"
The Quickstart configuration uses the `docker-compose.yml` file in the project root to stand up a non-production OSMT stack. This file builds a Docker image with Java 11 and Maven, builds the UI and API modules as a fat jar, and then stands up an application stack with the back-end dependencies (MySQL, ElasticSearch, and Redis) and a Spring application using the fat jar.

* This configuration could inform how to configure a production OSMT instance, but it is not intended for a production deployment.

The Quickstart configuration is deployed with a single docker-compose command. These steps below are the general process. Follow the guidance in [Environment files for Quickstart and Development Stacks](#environment-files-for-Quickstart-and-development-stacks) for more details.
  1. Create file named `osmt-quickstart.env` in the project root. This file will be ignored by git.
  2. From the project root, run this command:
     - `docker-compose --env-file osmt-quickstart.env up --build`
  3. Import any skills-related data you plan to use in your Quickstart instance
     - See [Importing Skills, BLS, and O*NET](./api/README.md#importing-skills-bls-and-onet) for details.
  4. Perform an initial index in ElasticSearch. OSMT will return an error if you skip this step.
      - From the `api` directory, run these commands:
          - `mvn clean package`
          - `mvn -Dspring-boot.run.profiles=dev,reindex springboot:run`
      - See [Elasticsearch indexing](./api/README.md#elasticsearch-indexing) for details.
  5. Open `http://localhost:8080` in your browser.

#### Development Configuration
The Development configuration uses the `dev-stack.yml` docker-compose file in the `docker` directory, for standing up just the back-end dependencies and doing active development in the Spring or Angular layers.
   1. Start the Development docker-compose stack (`dev-stack.yml`, with MySQL, ElasticSearch, and Redis).
      - Create a file named `osmt-dev-stack.env` in the `docker` directory. Follow the guidance in [Environment files for Quickstart and Development Stacks](#environment-files-for-Quickstart-and-development-stacks) for more details.
      - From the `docker` directory, run this command:
        - `docker-compose --env-file osmt-dev-stack.env dev-stack.yml up --build```

   2. Start the API Spring Boot application.
      - From the `api` directory, run these commands:
         - `mvn clean package`
         - `mvn -Dspring-boot.run.profiles=dev,apiserver,oauth2-okta springboot:run`
      - See [Spring Boot Configuration / Profiles](./api/README.md#spring-boot-configuration-profiles) in the [API README.md](./api/README.md) for specifics.

   3. Start the UI Angular front end. The Angular app is configured to proxy requests to the backend server during development. This allows one to use Angular's live reloading server.
      - From the `ui` directory, run these commands:
         - `npm install`
         - `npm run ng serve`
      - See [Spring Boot Configuration / Profiles](./api/README.md#spring-boot-configuration-profiles) in the [API README.md](./api/README.md) for specifics.

   4. Open `http://localhost:4200` in your browser.

   _For local development and CI/automated testing, you can use also the `noauth` Spring profile to bypass authentication._

### OAuth2 and Okta Configuration
To use Okta as your OAuth2 provider, you will need a free developer account with [Okta](https://okta.com). While the user interface at Okta may change, the big ideas of configuring an application for an OAuth/OpenID Connect provider should still apply. From your Okta Dashboard:
   1. Navigate to Applications. Create an Application Integration, using the OpenID Connect option. When prompted, choose the "Web Application" option.
   2. Provide a name that makes sense to you. The intention here is OSMT Quickstart and development.
   3. In the General tab, under "Client Credentials":
      - Copy/paste the values for Client ID and Client Secret into a text file, you will need them shortly.
   4. In the General tab, under "Login":
      - Enter `http://localhost:8080/login/oauth2/code/okta` for the Sign-in redirect URL.
      - Enter `http://localhost:8080` for the Sign-out redirect URL.
   5. In the Sign-On tab, under "OpenID Connect ID Token":
      -  Copy/paste the values for Issuer and Audience into the same text file.
   6. In the Assignments tab:
      - Assign your Okta user ID to your web application.
   7. OSMT does not require any scopes.

For Okta, you will use the `oauth2-okta` profile for Spring Boot, which will include the properties from [application-oauth2-okta.properties](./api/src/main/resources/config/application-oauth2-okta.properties). This properties file relies on secrets being provided via the environment.

#### Environment files for Quickstart and Development Stacks
There are many ways to provide environment values to a Spring application. That said, you should never push secrets to GitHub, so you should never store secrets in source code. The OSMT project is configured to git ignore files named `osmt*.env`, and we recommend you follow this approach. You can use files following this pattern to store your OAuth2 secrets locally and pass them to a docker-compose stack, e.g., `docker-compose --env-file /path/to/osmt.env -f /path/to/compose-file/yml up --build`. We suggest the following environment files:

  - Create a file named `osmt.env` in the project root. Populate it with these values and the relevant secrets from your OAuth2 provider. This will be provided to a Quickstart docker-compose stack from the docker-compose file in the project root directory.
    ```
    ENVIRONMENT=dev,apiserver,oauth2-okta
    BASE_DOMAIN=localhost:8080
    REDIS_URI=docker_redis_1:6379
    DB_URI=osmt_db_user:password@docker_db_1:3306
    ELASTICSEARCH_URI=http://docker_elasticsearch_1:9200
    MIGRATIONS_ENABLED=true
    OAUTH_ISSUER=https://abcdefg.okta.com
    OAUTH_CLIENTID=123456qwerty
    OAUTH_CLIENTSECRET=2354asdf
    OAUTH_AUDIENCE=3456zxcv
    ```

- Create a file named `osmt-dev-stack.env` in the docker directory. Populate it with these values and the relevant secrets from your OAuth2 provider. This will be provided to the Development docker-compose stack from the docker-compose file in the docker directory.
    ```
    OAUTH_ISSUER=https://abcdefg.okta.com
    OAUTH_CLIENTID=123456qwerty
    OAUTH_CLIENTSECRET=2354asdf
    OAUTH_AUDIENCE=3456zxcv
    ```

Also, you can provide these OAuth2 values as program arguments when starting your Spring Boot app (`-Dokta.oauth2.clientId="123456qwerty"`).

#### Running the Development stack from IntelliJ
IntelliJ can automate running different layers of the OSMT stack, and also provides valuable feedback about how they are running. These steps may inform how to work with OSMT in IntelliJ:
1. Import the top-level Maven project
    * Navigate to File -> New from existing sources
    * Select "Create from existing sources"
    * Select the project root folder
    * From IntelliJ's Maven panel, run the package phase on the osmt-parent (root) project.
2. Add a Run configuration for the local dev docker stack
   * Open the Project window to the docker directory. Right-click `dev-stack.yml` and choose "Edit configurations".
   * Provide a name that makes sense to you. The intention is an OSMT development stack.
   * Click "Modify" and add and environment file. Navigate to `osmt-dev-stack.env` and select it.
   * Save the Run configuration, and start it.
3. Use the [API README file](./api/README.md) to create a Run/Debug configuration for Spring Boot, and start it.
4. Use the [UI README file](./ui/README.md) to create Run/Debug configuration for Angular, and start it.
6. Open `http://localhost:4200` in your browser.

### Post-installation (BLS, O*NET, etc)
See the section for [Importing Skills, BLS, and O*NET](./api/README.md#importing-skills-bls-and-onet) in the API README file.

## How to get help
This project includes [./api/HELP.md](./api/HELP.md), with links to relevant references and tutorials.

OMST is an open source project, and is supported by its community. Please look to the Discussion boards and Wiki on GitHub. Please all see the [CONTRIBUTING.md](./CONTRIBUTING.md) document for additional context.


-------------------------------------------------

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
 * DB_URI
 * ELASTICSEARCH_URI
 
The use of these variables can be referenced in the [docker entrypoint script](docker/bin/docker_entrypoint.sh).

Example:
  ```
    ENVIRONMENT=review,apiserver
    BASE_DOMAIN=osmt.example.com
    REDIS_URI=<HOST>:<PORT>
    DB_URI=<USER>:<PASSWORD>@<HOST>:<PORT>
    ELASTICSEARCH_URI=<HOST>:<PORT>
  ```
