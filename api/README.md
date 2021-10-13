# OSMT API
This Maven module represents the Spring Boot backend application. 
 
## IntelliJ Setup
  * Open the project root into IntelliJ (parent folder of `./api`)
  * If prompted, import the `api` module, otherwise see `manually adding API module` below

### Manually Adding the API Module
  * Navigate to File -> New -> Module from existing sources
  * Select the `./api` folder where this README exists, in the file dialog
  * Select "Import module from external model" and choose "Maven"
  * Refresh Maven 
    
### Run/Debug Configurations
  Create a new Spring Boot configuration (Run -> Edit Configurations)
  * Click the plus icon for "Add New Configuration"
  * Select "Spring Boot"
  * For `Main class`, use `edu.wgu.osmt.Application`
  * Set `VM options` to `-Dspring.profiles.active=dev,apiserver,noauth`
  * Click 'ok'

## Spring Boot Configuration / Profiles
This project makes use of configuring Spring boot via property files. These are located at `./api/src/main/resources/config/`. A `dev` profile exists for local development, and can be applied by passing the `-Dspring.profiles.active=dev` argument on launch. Active profiles also control which Spring Boot `@Component`(s) are run.
  
### Override Specific Properties While Using Maven
To override specific properties with JVM arguments when developing with Maven, pass the JVM arguments as the value to `-Dspring-boot.run.jvmArguments=`

Example:  
 ```
 mvn -Dspring-boot.run.profiles=dev,apiserver,oauth2-okta \
 -Dspring-boot.run.jvmArguments="-Dspring.flyway.enabled=false" \
 spring-boot:run
```

| Configuration Profile     | Properties file           |
| -----------               | -----------               |
| (none)                    | application.properties    |
| dev                       | application-dev.properties|

| Component Profile         | Note                                                    |
| ---                       | ---                                                     |
| import                    | runs the batch import process, expects `--csv=` argument and `--import-type=` argument | 
| apiserver                 | runs the api server                                     |
| oauth2-okta               | includes required configuration for OAuth2 OIDC with Okta|
| reindex                   | runs the Elasticsearch re-index process |

For example to run the import component with a dev configuration, set active profiles by passing a JVM argument like this:
`-Dspring-boot.run.profiles=dev,import`

## OAuth2 
An example profile and Spring Boot components (edu.wgu.osmt.security.SecurityConfig) are provided to support OAuth2 with Okta. To use a different provider, create a separate Spring Boot profile to contain the configuration. Additional Spring Boot components may also be required to support the chosen provider. See [OAuth2 and Okta Configuration](../README.md#oauth2-and-okta-configuration) in the [README](../README.md) for more details.

## Database Configurations
This project uses [FlywayDb](https://flywaydb.org/). SQL Migrations can be placed in `./api/src/main/resources/db/migration/`.
Scripts in this folder will be automatically processed when the app is started with the appropriate `application.properties` settings in `spring.flyway.*`.

By default, only `test` and `dev` environments will automatically run migrations. To enable migrations for other environments, i.e. a single production server, start the server with this JVM argument: `-Dspring.flyway.enabled=true`. 

## Code Style
To automatically apply the official Kotlin code style, Install the IntelliJ plugin `Save Actions`. Configure `Save Actions` in preferences to `Reformat file` on save.    

## Command Line execution
#### Building the jar:
```mvn clean package```

#### Run the web service:
```mvn -Dspring-boot.run.profiles=dev,apiserver spring-boot:run```

#### Running the web service from the jar:
```java -Dspring.profiles.active=dev,apiserver -jar api/target/osmt-api-<version>.jar```

## Importing Skills, BLS, and O*NET
OSMT allows importing BLS and O*NET data into it's MySQL database and Elasticsearch indexing. If you are going to import this data, you should do it before deploying the Spring Boot web application. The import process supports CSV files. See specific information for each type below.

The general import sequence should be: 
1. Batch Skills (```--import-type=batchskill```)
2. BLS (```--import-type=bls```)
3. O*NET (```--import-type=onet```)
4. Reindex Elastisearch

### Running batch skill / collection import from the jar:
```
java -jar -Dspring.profiles.active=dev,import api/target/osmt-api-<version>.jar --csv=path/to/csv    
```

### Importing BLS codes:
* Note - BLS codes should be imported before O*NET codes
1. Download BLS codes in Excel format from [https://www.bls.gov/soc/2018/#materials]("https://www.bls.gov/soc/2018/#materials")
2. Convert Excel to CSV format
3. Import the CSV with either the following command:
    ```java -jar -Dspring.profiles.active=dev,import api/target/osmt-api-<version>.jar --csv=path/to/bls_csv --import-type=bls```
4. You can also create a Run configuration in IntelliJ
    1. Add a Spring Boot Run configurations in IntelliJ.
    2. In VM options, enter ```-Dspring.profiles.active=dev,import```
    3. In program arguments, enter ```--csv=path/to/bls_csv --import-type=bls ```
    4. Save and then run the app.

### Importing O*NET
* Note - BLS codes should be imported before O*NET codes
1. Download O*NET `Occupation Data` in Excel format from [https://www.onetcenter.org/database.html#occ]("https://www.onetcenter.org/database.html#occ")
2. Convert Excel to CSV format
3. Import the CSV with the following command:
    ```
    java -jar -Dspring.profiles.active=dev,import api/target/osmt-api-<version>.jar --csv=path/to/onet_csv --import-type=onet    
    ```
4. You can also create a Run configuration in IntelliJ
    1. Add a Spring Boot Run configurations in IntelliJ.
    2. In VM options, enter ```-Dspring.profiles.active=dev,import```
    3. in program arguments type in this ```--csv=path/to/onet_csv --import-type=onet ```
    4. Save and then run the app.

## Elasticsearch indexing
After the initial import, it is necessary to run Spring Boot with the `reindex` profile to generate Elasticsearch index mappings and documents. The `<environment profile>` in the following examples can be `dev`,`review` or omitted for production. 

Via Maven:
```
mvn -DSpring.profiles.active=<environment profile>,reindex
```
Via the compiled jar:
```
java -Dspring.profiles.active=<environment profile>,reindex -jar api/target/osmt-api-<version>.jar 
``` 


### Reindex after changes to Elasticsearch `@Document` index classes
If changes are made to @Document annotated classes, the indexes need to be deleted and re-indexed. 
* Delete all of the indices in Elasticsearch by executing the following:
    ```
    curl -X DELETE "http://<elasticsearch host>:<elasticsearch port>/*" 
    ``` 
    Where `host` and `port` represent the Elasticsearch server you are targeting, e.g. `localhost:9200` 
* Run the reindex command from above to generate the new mappings and documents
