# WGU Open Skills Management Toolset (OSMT)

## Overview
The Open Skills Management Tool (OSMT, pronounced "oz-mit") is a free, open-source instrument to facilitate the production of Rich Skill Descriptor (RSD) based open skills libraries. In short, it helps to create a common skills language by creating, managing, and organizing skills-related data. An open-source framework allows everyone to use the tool collaboratively to define the RSD, so that those skills are translatable and transferable across educational institutions and hiring organizations within programs, curricula, and job descriptions.

## Architecture
OSMT is written in Kotlin with Spring Boot, and Angular. It uses backend-instances of MySQL, Redis, and Elastisearch.
![High-level architecture diagram](./ui/src/assets/Architectural-Diagram.png "High-level architecture diagram")

### Dependencies
OSMT uses Elasticsearch, Redis, and MySQL as back-end dependencies. Any OSMT instance beyond a local development server also requires an OAuth2 provider. Local /non-production configurations can be stood up using only Docker. See additional notes below for [Configuration](#configuration).

## Development Utilities
The OSMT source code is bundled with a utility to ease getting acclimated to OSMT's local environment and with managing local instances. This utility is named `osmt_dev.sh` and exists in the project root directory. `osmt_dev.sh` requires BASH, and work on MacOS and Linux. It may work with a BASH interpreter in Windows, but we have not yet tested this. `osmt_dev.sh` requires exactly one flag (listed below). Calling `./osmt_dev.sh -h` returns this help text:
```
Usage:
  osmt_dev.sh [accepts a single option]

  -i   Initialize environment files for Quickstart and Development configurations.
  -v   Validate local environment and dependencies for development.
  -q   Start the Quickstart configuration. Application and services are containerized and
       managed by docker-compose. The docker-compose stack will attach to the console, with containers
       named "osmt_".
  -d   Start the backend Development Docker stack (MySQL, ElasticSearch, Redis). docker-compose stack will
       be detached, with containers named "osmt_dev_". You can review status with 'docker ps'
  -e   Stop the detached backend Development Docker stack (MySQL, ElasticSearch, Redis).
  -s   Start the local Spring app, as built from source code. This also sources the api/osmt-dev-stack.env file
       for OAUTH2-related environment variables.
  -m   Import default BLS and O*NET metadata into local Development instance
  -c   Surgically clean up OSMT-related Docker images and data volumes. This step will delete data from local OSMT
       Quickstart and Development configurations. It does not remove the mysql/redis/elasticsearch images, as
       those may be available locally for other purposes.
  -h   Show this help message.
```

### Getting starting with the OSMT development utility (`osmt_dev.sh`)
Use this workflow to help bootstrap a local Quickstart OSMT instance (you can see more in the [Quickstart Configuration](#quickstart-configuration) section).
*  A Quickstart OSMT instance should be for demo purposes only. The data is stored on a Docker volume, on the same computer where you started the Quickstart. You should consider this data to be temporary. If you create RSDs or Collections, you should export them before shutting down the Quickstart. Please use a deployed OSMT instance when making any real investment in effort for building skills.

1. Initialize your environment files. After running this, update the OAUTH2/OIDC values in the env files (replace the `xxxxxx` values with the correct values from your OAUTH 2/OIDC provider)
    ```
    osmt_dev.sh -i
    ``` 
2. Validate your local environment (for Docker, Java, and other SDKs / runtimes). If this command reports error, you will need to resolve them before running the Quickstart configuration. See more in the [Requirements to Build OSMT](#requirements-to-build-osmt) section
    ```
    osmt_dev.sh -v
    ```

#### Running the Quickstart with `osmt_dev.sh`
1. Start the Quickstart configuration. This will take several minutes to download resources and build the software. If you continue to loop on errors that report "Retrying in 10 seconds...", there's something wrong. Search and reach out in the [Discussion](https://github.com/wgu-opensource/osmt/discussions) boards in GitHub.
    ```
    osmt_dev.sh -q
    ``` 

2. You can exit the Quickstart by pressing [Ctrl-C]. If you don't clean up the Docker images and volumes, Quickstart should start very quickly when re-running `osmt_dev.sh -q`.   

#### Running the Development configuration with `osmt_dev.sh`
1. From the project root, run a Maven build. The API module currently depends on the UI module, so 'mvn package' will not be sufficient.
    ```
    mvn clean install
    ``` 

2. Start the back-end MySQL/Redis/ElasticSearch dependencies. After this command, the Docker stack will run "detached", meaning running in the background. You can use `docker ps` and `docker logs` to see what the services are doing.
    ```
    osmt_dev.sh -d
    ``` 

3. If you prefer, you can import BLS and O*NET job code metadata. This step is optional.
    ```
    osmt_dev.sh -m
    ``` 

4. Start the Spring application. It will also serve the static Angular files. You can exit the Spring application by pressing [Ctrl-C].
    ```
    osmt_dev.sh -s
    ``` 

5. Shut down the back-end MySQL/Redis/ElasticSearch dependencies.
    ```
    osmt_dev.sh -e
    ``` 

#### Housekeeping with `osmt_dev.sh`
You can surgically clean up OSMT-related Docker images and data volumes. This step **will** delete data from local OSMT Quickstart and Development configurations. It does not remove the mysql/redis/elasticsearch images, as those may be available locally for other purposes.
    ```
    osmt_dev.sh -c
    ```

### Requirements to Build OSMT Locally
OSMT requires certain software and SDKs to build:
* Docker >=17.06.0
  * Recommended 4 GB memory allocated to the Docker service
* a Java 11 JDK (OpenJDK works fine)
* Maven 3.8.3 or higher
* NodeJS v16.13.0 / npm 8.1.0 or higher (the LTS versions at the time of this writing)
  * NodeJS/npm are used for building client code; there are no runtime NodeJS/npm dependencies.
  * In the `ui` module, `frontend-maven-plugin` uses an embedded copy of Node v16.13.0 and npm 8.1.0.
  * Locally, a developer probably has their own versions of NodeJS and npm installed. They should be >= the versions given above.

Run `osmt_dev.sh -v` for feedback on your local SDKs and runtimes. The output of this command may be helpful in troubleshooting with the OSMT community as well.

### Project Structure
OSMT is a multi-module Maven project. pom.xml files exist in the project root, `./api` and `./ui` directories. Running the command `mvn clean install` from the project root will create a fat jar in the target directory that contains both the backend server and the prod-built Angular frontend static files.

    project root/
    |-- api                - Spring Boot, Kotlin-based backend
    |-- ui                 - Angular frontend
    \-- docker             - Misc. Docker support for development

* `mvn package` may work fine, but the `api` module depends on artifacts from the `ui` module. `mvn install` will place the ui artifacts in your local Maven repo, and will decouple your local builds from the presence of the jar file being present in `ui/target`

The [API](./api/README.md) and [UI](./ui/README.md) modules have their own README.md files with more specific information about those layers.

### Configuration
* This project makes use of 2 similar local configurations, labelled in this documentation as "Quickstart" and "Development". These both rely on docker-compose.
* OSMT requires an OAuth2 provider. It is preconfigured for Okta, but you can use any provider.

1. Run `osmt_dev.sh -i` to initialize the required environment files `osmt-quickstart.env` in the project root. This file will be ignored by git.
2. Run `osmt_dev.sh -v` to validate that your machine has the correct software to run the Quickstart

#### Quickstart Configuration
The Quickstart configuration uses the `docker-compose.yml` file in the project root to stand up a non-production OSMT stack. This file uses Docker images with Java 11 and Maven to build the UI and API modules as a fat jar, and then stands up an application stack with the back-end dependencies (MySQL, ElasticSearch, and Redis) and a Spring application using the fat jar. The Quickstart configuration automatically imports BLS and O*NET job code metadata

* This configuration could inform how to configure a production OSMT instance, but it is not intended for a production deployment.

The Quickstart configuration is deployed with a single docker-compose command. These steps below are the general process. Follow the guidance in [Environment files for Quickstart and Development Stacks](#environment-files-for-quickstart-and-development-stacks) for more details.

1. Run `osmt_dev.sh -q` to start the Quickstart configuration. This may take several minutes to download dependencies and build tte software.
2. Open `http://localhost:8080` in your browser.

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

The OSMT source code includes example environment files for the Quickstart and Development configurations (./osmt-quickstart.env.example and ./api/osmt-dev-stack.env.example) Run this command to create your environment files. It will not overwrite existing OSMT environment files.
  ```./osmt_dev.sh -i```
Then replace the 'xxxxxx' values with your OAUTH2/OIDC values, following the guidance in the [OAuth2 and Okta Configuration](#oauth2-and-okta-configuration) section.

#### Quickstart
Use the `./osmt-quickstart.env` file to pass your OAuth2 secrets to the Quickstart docker-compose stack, e.g., `docker-compose --env-file /path/to/osmt.env -f /path/to/compose-file/yml up --build`.

#### Development Stacks
Use the `./api/osmt-dev-stack.env` file to pass your OAuth2 secrets to the local Spring application. Do this by sourcing the 


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
The Quickstart configuration automatically has the default BLS and O*NET job code metadata imported. For a Development configuration, you can import the metadata with this command:
```
osmt_dev.sh -m
```
For more information, see the section for [Importing Skills, BLS, and O*NET](./api/README.md#importing-skills-bls-and-onet) in the API README file.

## How to get help
This project includes [./api/HELP.md](./api/HELP.md), with links to relevant references and tutorials.

OMST is an open source project, and is supported by its community. Please look to the Discussion boards and Wiki on GitHub. Please all see the [CONTRIBUTING.md](./CONTRIBUTING.md) document for additional context.
