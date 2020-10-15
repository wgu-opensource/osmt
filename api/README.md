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

| Configuration Profile     | Properties file           |
| -----------               | -----------               |
| (none)                    | application.properties    |
| dev                       | application-dev.properties|

| Component Profile         | Note                                                    |
| ---                       | ---                                                     |
| import                    | runs the batch import process, expects `--csv=` argument | 
| apiserver                 | runs the api server                                     |
| oauth2                    | includes required configuration for oauth2 oidc with okta|

For example to run the import component with a dev configuration, set active profiles by passing a JVM argument like so:
`-Dspring-boot.run.profiles=dev,import`

### OAuth2 Okta configuration
To use okta as your OAuth2 provider you will need to provide the following properties when running the application. These can be found in your okta server configuration.
 * okta.oauth2.clientId      
 * okta.oauth2.clientSecret
 * okta.oauth2.audience
 * okta.oauth2.issuer

  
## Database migrations
This project uses [FlywayDb](https://flywaydb.org/). SQL Migrations can be placed in `./api/src/main/resources/db/migration/`.
Scripts in this folder will be automatically processed when the app is ran with the appropriate `application.properties` settings in `spring.flyway.*` 

## Code style
To automatically apply the official Kotlin code style, Install the IntelliJ plugin `Save Actions`. Configure `Save Actions` in preferences to `Reformat file` on save.    

## Command Line execution
#### Building the jar:
```mvn clean package -Dmaven.test.skip.exec```

#### Run the web service:
```mvn -Dspring-boot.run.profiles=dev,apiserver spring-boot:run```

#### Running the web service from the jar:
```java -Dspring.profiles.active=dev,apiserver -jar api/target/osmt-api-0.0.1-SNAPSHOT.jar```

#### Running batch import from the jar:
```
java -jar -Dspring.profiles.active=dev,import api/target/osmt-api-0.0.1-SNAPSHOT.jar --csv=path/to/csv    
```
