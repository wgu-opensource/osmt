# OSMT API
This module represents the Spring Boot backend application. 
 
## IntelliJ setup
  * Open the project root into IntelliJ (parent folder of `./api`)
  * If prompted, import the `api` module, otherwise see `manually adding API module` below

### Manually adding the API module
  * Navigate to File -> New -> Module from existing sources
  * Select the `./api` folder where this README exists, in the file dialog
  * Select "Import module from external model" and choose "Maven"
  * Refresh Maven 
    
### Run/Debug Configurations
  Create a new Spring Boot configuration (Run -> Edit Configurations)
  * Click the plus icon for "Add New Configuration"
  * Select "Spring Boot"
  * For `Main class`, use `edu.wgu.osmt.Application`
  * Set `VM options` to `-Dspring.profiles.active=dev,apiserver`
  * Click 'ok'

## Spring Boot configuration / Profiles
This project makes use of configuring Spring boot via property files. These are located at `./api/src/main/resources/config/`. A `dev` profile exists for 
  local development, and can be applied by passing the `-Dspring.profiles.active=dev` argument on launch. Active profiles also control what Spring Boot `@component`(s) are run.
  
### Override specific properties while using Maven
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

For example to run the import component with a dev configuration, set active profiles by passing a JVM argument like so:
`-Dspring-boot.run.profiles=dev,import`

### OAuth2 
An example profile and Spring Boot components (edu.wgu.osmt.security.SecurityConfig) are provided to support OAuth2 with Okta. To use a different provider, create a separate Spring Boot profile to contain the configuration.  
Additional Spring Boot components may also be required to support the chosen provider.

#### Okta configuration
To use Okta as your OAuth2 provider, include `oauth2-okta` in the list of Spring Boot profiles. You will need to provide the following properties when running the application. These can be found in your Okta server configuration.
 * okta.oauth2.clientId      
 * okta.oauth2.clientSecret
 * okta.oauth2.audience
 * okta.oauth2.issuer

  
## Database migrations
This project uses [FlywayDb](https://flywaydb.org/). SQL Migrations can be placed in `./api/src/main/resources/db/migration/`.
Scripts in this folder will be automatically processed when the app is ran with the appropriate `application.properties` settings in `spring.flyway.*`. 

By default only `test` and `dev` environments will automatically run migrations. To enable migrations for other environments, i.e. a single production server, include the JVM argument `-Dspring.flyway.enabled=true`
when running the server. 

## Code style
To automatically apply the official Kotlin code style, Install the IntelliJ plugin `Save Actions`. Configure `Save Actions` in preferences to `Reformat file` on save.    

## Command Line execution
#### Building the jar:
```mvn clean package -Dmaven.test.skip.exec```

#### Run the web service:
```mvn -Dspring-boot.run.profiles=dev,apiserver spring-boot:run```

#### Running the web service from the jar:
```java -Dspring.profiles.active=dev,apiserver -jar api/target/osmt-api-<version>.jar```



## Imports
Order of imports should be batch skills, BLS, O*NET

### Running batch skill / collection import from the jar:
```
java -jar -Dspring.profiles.active=dev,import api/target/osmt-api-<version>.jar --csv=path/to/csv    
```
### BLS codes
Note, BLS codes should be imported before O*NET codes
1) Download BLS codes in Excel format from [https://www.bls.gov/soc/2018/#materials]("https://www.bls.gov/soc/2018/#materials")
2) Convert Excel to CSV format
3) Import the CSV with the following command:
    ```
    java -jar -Dspring.profiles.active=dev,import api/target/osmt-api-<version>.jar --csv=path/to/bls_csv --import-type=bls    
    ```

### O*net codes
Note, BLS codes should be imported before O*NET codes
1) Download O*NET `Occupation Data` in Excel format from [https://www.onetcenter.org/database.html#occ]("https://www.onetcenter.org/database.html#occ")
2) Convert Excel to CSV format
3) Import the CSV with the following command:
    ```
    java -jar -Dspring.profiles.active=dev,import api/target/osmt-api-<version>.jar --csv=path/to/onet_csv --import-type=onet    
    ```

## Elasticsearch indexing
After the initial import, it is necessary to run spring boot with the `reindex` profile to generate Elasticsearch index mappings and documents. Where `<environment profile>` in the following examples can be `dev`,`review` or omitted for production. 

Via the compiled jar:
```
java -Dspring.profiles.active=<environment profile>,reindex -jar api/target/osmt-api-<version>.jar 
``` 

Via mvn:
```
mvn -DSpring.profiles.active=<environment profile>,reindex
```

### Reindex after changes to Elasticsearch `@Document` index classes
If changes are made to @Document annotated classes, the indexes need to be deleted and re-indexed. 
* Delete all of the indices in Elasticsearch by executing the following:
    ```
    curl -X DELETE "http://<elasticsearch host>:<elasticsearch port>/*" 
    ``` 
    Where `host` and `port` represent the Elasticsearch server you are targetting, e.g. `localhost:9200` 
* Run the reindex command from above to generate the new mappings and documents
