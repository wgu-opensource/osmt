# WGU Open Skills Management Toolset (OSMT)

## Project Structure
    project root/
    |-- api                - Spring Boot, Kotlin based backend - See `./api/README.md`
    |-- ui                 - Angular frontend - See `./ui/README.md`
    |-- docker             - Misc. Docker support for development

## Getting started
This project is a multi module Maven project. Pom.xml files exist in the project root, ./api and ./ui.  
  * Import module
      * Navigate to File -> New -> Module from existing sources
      * Select "Create module from existing sources"
      * Select the root project folder where this README exists from the file dialog
  * Go through the readme files of `./api` and `./ui` to setup the child modules in IntelliJ and to create run/debug configurations 
  * Start the docker development stack for MySQL and other dependencies
    * from the `./docker` folder, run `docker-compose -f dev-stack.yml up`
    * to shut down the development stack, press `<ctl> + c`  
  * Run both the frontend and backend configurations you created from IntelliJ
  * Visit `http://localhost:4200`

## Local Development
The Angular UI app is configured to proxy requests to the backend server during development. This allows one to use Angular's live reloading server.

## Building
Running the command `mvn clean install` will result in a fat jar being built that contains both the backend server and the built Angular frontend static files.

## Other notes
The backend will serve any routes not already configured to the API to the frontend, allowing Angular's routing to takeover. 

## Deploy steps
### CSV import

1. clone github repository
1. copy docker/mysql-init/ to a host which can connect to mysql database
1. Run the .sql files against mysql database
  1. mysql -u <DB_USER> -p -h <DB_HOST> -P 3306 < osmt_initial.sql
1. Run the app container: `docker run -ti --entrypoint /bin/bash -v <full_path_csv_folder>:/mnt concentricsky/osmt:0.5.0
1. Run the csv import:
```
cd /mnt/
/bin/java \
  -Dspring.profiles.active=review,import \
  -Dredis.uri=<REDIS_HOST>:<REDIS_PORT> \
  -Dapp.baseDomain=<BASE_DOMAIN_NAME> \
  -Ddb.uri=<DB_USER>:<DB_PASS>@<DB_HOST>:3306 \
  -Des.uri=<HOST>:<PORT> \
  -jar /opt/osmt/bin/osmt.jar \
  --csv=/mnt/<PATH_TO_CSV>
```

### Run The Container

1. Run the container and pass the following environment variables to it: ENVIRONMENT, ENVIRONMENT_DOMAIN_NAME, REDIS_URI, MYSQL_DB_URI, ELASTICSEARCH_URI. The use of these variables can be references in the [docker entrypoint script](docker/bin/docker_entrypoint.sh):
  1. Example enviroment variables:
  ```
    ENVIRONMENT=apiserver,review
    ENVIRONMENT_DOMAIN_NAME=<BASE_DOMAIN_NAME>
    REDIS_URI=<HOST>:<PORT>
    MYSQL_DB_URI=<USER>:<PASSWORD>@<HOST>:<PORT>
    ELASTICSEARCH_URI=<HOST>:<PORT>
  ```
