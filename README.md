# WGU Open Skills Management Toolset (OSMT)

## Overview
The Open Skills Management Tool (OSMT, pronounced "oz-mit") is a free, open-source instrument to facilitate the production of Rich Skill Descriptor (RSD) based open skills libraries. In short, it helps to create a common skills language by creating, managing, and organizing skills-related data. An open-source framework allows everyone to use the tool collaboratively to define the RSD, so that those skills are translatable and transferable across educational institutions and hiring organizations within programs, curricula, and job descriptions.

## Architecture
OSMT is written in Kotlin with Spring Boot, and Angular. It uses backend-instances of MySQL, Redis, and Elastisearch.
![OSMT architectural overview](./ui/src/assets/Architectural-Diagram.png)

### Dependencies
OSMT uses Elasticsearch, Redis, and MySQL as back-end dependencies. Any OSMT instance beyond a local development server also requires an OAuth2 provider. See additional notes below for [Configuration](#configuration).

### Requirements to Build OSMT
OSMT requires certain software and SDKs to build:
* a Java 11 JDK (OpenJDK works fine)
* Maven 3.8.3 or higher
* NodeJS v10.16.0 / npm 6.10.2 or higher
  * NodeJS/npm are used for building client code; there are no runtime NodeJS/npm dependencies.
  * Maven uses an embedded copy of Node v10.16.0 and npm 6.10.2 (see [About frontend-maven-plugin](./ui/README.md#about-frontend-maven-plugin) in the UI README file).
  * Locally, a developer probably has their own versions of NodeJS and npm installed. They should be >= the versions given above.
* a recent version of Docker and docker-compose
  * Recommended 4 GB memory allocated to the Docker service

### Project Structure
OSMT is a multi-module Maven project. pom.xml files exist in the project root, `./api` and `./ui` directories. Running the command `mvn clean install` from the project root will create a fat jar in the target directory that contains both the backend server and the prod-built Angular frontend static files.

    project root/
    |-- api                - Spring Boot, Kotlin-based backend
    |-- ui                 - Angular frontend
    \-- docker             - Misc. Docker support for development
* `mvn package` may work file, but the `api` module depends on artifacts from the `ui` module. `mvn install`. Will place the ui artifacts in your local Maven repo, and will decouple your local builds from the present of the jar file being present in `ui/target`

The [API](./api/README.md) and [UI](./ui/README.md) modules have their own README.md files with more specific information about those layers.

### Configuration
* This project makes use of 2 similar configurations, labelled in this documentation as "Quickstart" and "Development".
* OSMT requires an OAuth2 provider. It is preconfigured for Okta, but you can use any provider.

#### Quickstart Configuration

The Quickstart configuration uses the `docker-compose.yml` file in the project root to stand up a non-production OSMT stack. This file builds a Docker image with Java 11 and Maven, builds the UI and API modules as a fat jar, and then stands up an application stack with the back-end dependencies (MySQL, ElasticSearch, and Redis) and a Spring application using the fat jar.

* This configuration could inform how to configure a production OSMT instance, but it is not intended for a production deployment.

The Quickstart configuration is deployed with a single docker-compose command. These steps below are the general process. Follow the guidance in [Environment files for Quickstart and Development Stacks](#environment-files-for-quickstart-and-development-stacks) for more details.

1. Create file named `osmt.env` in the project root. This file will be ignored by git.
2. From the project root, run this command:
  - `docker-compose --env-file osmt.env up --build`
3. Import any skills-related data you plan to use in your Quickstart instance.
  - Replace the content in the existing files in import folder for skills, BLS and O*NET data. __These existing files under import folder are provided for testing purpose only.__
  - Run below commands.
    ```
    docker exec -it osmt_app_1 /bin/bash
    java -jar -Dspring.profiles.active=dev,import /opt/osmt/bin/osmt.jar --csv=/opt/osmt/import/BLS-Import.csv --import-type=bls
    java -jar -Dspring.profiles.active=dev,import /opt/osmt/bin/osmt.jar --csv=/opt/osmt/import/oNet-Import.csv --import-type=onet  
    java -Dspring.profiles.active=dev,reindex -jar /opt/osmt/bin/osmt.jar
    ```
  - See [Importing Skills, BLS, and O*NET](./api/README.md#importing-skills-bls-and-onet) for details.
4. Open `http://localhost:8080` in your browser.

#### Development Configuration
The Development configuration uses the `dev-stack.yml` docker-compose file in the `docker` directory, for standing up just the back-end dependencies and doing active development in the Spring or Angular layers.
1. Start the Development docker-compose stack (`dev-stack.yml`, with MySQL, ElasticSearch, and Redis).
  - From the `docker` directory, run this command:
    - `docker-compose -f dev-stack.yml up`

2. Start the API Spring Boot application.
- From the `project root` directory, run this commands
    - `mvn clean install`
- Create a file named `osmt-dev-stack.env` in the `api` directory. Follow the guidance in [Environment files for Quickstart and Development Stacks](#environment-files-for-quickstart-and-development-stacks) for more details.
- From the `api` directory, run this command:
    - `mvn -Dspring-boot.run.profiles=dev,apiserver,oauth2-okta spring-boot:run`
- See [Spring Boot Configuration / Profiles](./api/README.md#spring-boot-configuration-profiles) in the [API README.md](./api/README.md) for specifics.

3. Start the UI Angular front end. The Angular app is configured to proxy requests to the backend server during development. This allows one to use Angular's live reloading server.
  - From the `ui` directory, run these commands:
    - `npm install`
    - `npm run ng serve`
    
4. Open `http://localhost:4200` in your browser.

_For local development testing of API module, you can use also the `noauth` Spring profile to bypass authentication._

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
There are many ways to provide environment values to a Spring application. That said, you should never push secrets to GitHub, so you should never store secrets in source code. The OSMT project is configured to git ignore files named `osmt*.env`, and we recommend you follow this approach. 

You can use files that follow this pattern to store your OAuth2 secrets locally and pass them to a docker-compose stack, e.g., `docker-compose --env-file /path/to/osmt.env -f /path/to/compose-file/yml up --build`. We suggest the following environment files:

- Create a file named `osmt.env` in the project root. Populate it with these values and the relevant secrets from your OAuth2 provider. This will be provided to a Quickstart docker-compose stack from the docker-compose file in the project root directory. In a Linux/MacOS environment, you can also source these environment variables into your shell
  ```
  set -o allexport

  ENVIRONMENT=dev,apiserver,oauth2-okta
  BASE_DOMAIN=localhost:8080
  FRONTEND_URL=http://localhost:8080
  
  # this is optional. Only provide it if you plan to whitelabel OSMT
  OSMT_WHITELABEL_URL=/whitelabel/whitelabel-my-org.json

  DB_NAME=osmt_db
  DB_USER=osmt_db_user
  DB_PASSWORD=password
  MYSQL_ROOT_PASSWORD=root_password
  MIGRATIONS_ENABLED=true
  REINDEX_ELASTICSEARCH=true

  ELASTICSEARCH_URI=http://osmt_elasticsearch_1:9200
  REDIS_URI=osmt_redis_1:6379

  OAUTH_ISSUER=https://abcdefg.okta.com
  OAUTH_CLIENTID=123456qwerty
  OAUTH_CLIENTSECRET=2354asdf
  OAUTH_AUDIENCE=3456zxcv
  
  set +o allexport
  ```

#### Development Stacks
- Create a file named `osmt-dev-stack.env` in the api directory. Populate it with these values and the relevant secrets from your OAuth2 provider.
    ```
    OAUTH_ISSUER=https://<okta_issuer>.okta.com
    OAUTH_CLIENTID=<oauth_clientId>
    OAUTH_CLIENTSECRET=<oauth_client_secret>
    OAUTH_AUDIENCE=<oauth_audience>
    ```
- Create a bash file say exportEnv.sh in API folder to supply env properties
    ```
  #!/bin/bash
    
  echo -n "Starting Spring Boot from Maven"
    
  declare ENV_FILE=$1
    
  if [[ -n "${ENV_FILE}" ]]; then
   echo " using environment variables from ${ENV_FILE}"
   set -o allexport
   source "${ENV_FILE}"
   set +o allexport
  fi
  
  mvn -Dspring-boot.run.profiles=dev,apiserver,oauth2-okta spring-boot:run
  ```
- Provide 755 permission to exportEnv.sh file 
  ```
  chmod 755 exportEnv.sh
  ```
- run``` ./exportEnv.sh osmt-dev-stack.env```
- Alternate approaches to supply env variables. 
  - you can provide these OAuth2 values as program arguments when starting your Spring Boot app (`-Dokta.oauth2.clientId="123456qwerty"`).
  - Set environment variables by supplying json
    - In Windows, replace <placeholder> values and run below command.
      ```
      set SPRING_APPLICATION_JSON="{\"OAUTH_ISSUER\":\"<aouth_issuer>\",\"OAUTH_CLIENTID\":\"<aouth_client>\", \"OAUTH_CLIENTSECRET\":\"<oauth_clientsecret>\",\"OAUTH_AUDIENCE\":\"<oauth_audience>\"}"
      ```
    - In mac & linux, replace <placeholder> values and run below command.
      ```
      export SPRING_APPLICATION_JSON="{\"OAUTH_ISSUER\":\"<aouth_issuer>\",\"OAUTH_CLIENTID\":\"<aouth_client>\", \"OAUTH_CLIENTSECRET\":\"<oauth_clientsecret>\",\"OAUTH_AUDIENCE\":\"<oauth_audience>\"}"
      ```

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

## Troubleshooting Instructions
- Scenario 1
- Scenario 2



