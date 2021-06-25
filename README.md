# WGU Open Skills Management Toolset (OSMT)

## Project Structure
    project root/
    |-- api                - Spring Boot, Kotlin based backend - See `./api/README.md`
    |-- ui                 - Angular frontend - See `./ui/README.md`
    |-- docker             - Misc. Docker support for development


![OSMT architectural overview](./ui/src/assets/Architectural-Diagram.png)
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

## BLS & Onet codes
You need to make sure you install these codes before deploying, you can find the codes for BLS [here](https://www.bls.gov/soc/2018/#materials)
and the Onet codes [here](https://www.onetcenter.org/database.html#occ). Make sure you export them as CSV files.
*NOTE*, make sure you have mysql setup before continuing with the following steps. After downloading them follow these steps:
### steps to import BLS codes:
1. go to edit configurations at the top bar of IntelliJ and click on springboot -> Application.
2. in VM options type in this ```-Dspring.profiles.active=dev,import```
3. in program arguments type in this ```--csv=path/to/bls_csv --import-type=bls ```
4. click apply and ok and then run the app.

### for importing Onet
1. go to edit configurations at the top bar of IntelliJ and click on springboot -> Application.
2. in VM options type in this ```-Dspring.profiles.active=dev,import```
3. in program arguments type in this ```--csv=path/to/onet_csv --import-type=onet ```
4. click apply and ok and then run the app.

## Okta configuration
To use Okta as your OAuth2 provider, include `oauth2-okta` in the list of Spring Boot profiles. You will need to provide the
following properties when running the application. To get these properties you will need go to okta, create an
Okta application in Web Mode with OpenID. Once created you can find the values on the left nav, click "Applications->Applications".
Then, in the main content pane, select the application you created. You will immediately be presented with
the Client ID and Client Secret. Clicking on the "Sign On" tab will present you with the issuer and audience values.
 * okta.oauth2.clientId      
 * okta.oauth2.clientSecret
 * okta.oauth2.audience
 * okta.oauth2.issuer
 
## Local Development
The Angular UI app is configured to proxy requests to the backend server during development. This allows one to use Angular's live reloading server.

## Building
Running the command `mvn clean install` will result in a fat jar being built that contains both the backend server and the built Angular frontend static files.

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

