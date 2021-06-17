# WGU Open Skills Management Toolset (OSMT)

## Project Structure
    project root/
    |-- api                - Spring Boot, Kotlin based backend - See `./api/README.md`
    |-- ui                 - Angular frontend - See `./ui/README.md`
    |-- docker             - Misc. Docker support for development

for a visual over view checkout out the lucidchart diagram [here](https://lucid.app/lucidchart/be7b8312-4ad5-4713-9e3c-a104dd5c5c80/edit?beaconFlowId=48DAEE0D049C7111&page=0_0#)
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
NOTE, make sure
you have the right mysql credentials. after downloading them follow these steps to import BLS codes:
1. go to edit configurations at the top bar of IntelliJ and click on springboot -> Application.
2. in VM options type in this ```-Dspring.profiles.active=dev,import```
3. in program arguments type in this ```--csv=path/to/bls_csv --import-type=bls ```
4. click apply and ok and then run the app.

for importing Onet follow these steps
1. go to edit configurations at the top bar of IntelliJ and click on springboot -> Application.
2. in VM options type in this ```-Dspring.profiles.active=dev,import```
3. in program arguments type in this ```--csv=path/to/onet_csv --import-type=onet ```
4. click apply and ok and then run the app.

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

