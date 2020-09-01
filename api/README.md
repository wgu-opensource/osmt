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
  * Set `VM options` to `-Dspring.profiles.active=dev`
  * Click 'ok'
  
### Spring Boot configuration
  This project makes use of configuring Spring boot via property files. These are located at `./api/src/main/resources/config/`. A `dev` profile exists for 
  local development, and can be applied by passing the `-Dspring.profiles.active=dev` argument on launch. 
  
### Database migrations
  This project is configure to use [FlywayDb](https://flywaydb.org/). SQL Migrations can be placed in `./api/src/main/resources/db/migration/`.
  Scripts in this folder will be automatically processed when the app is run with the appropriate `application.properties` settings in `spring.flyway.*` 


### Code style
To automatically apply the official Kotlin code style, Install the IntelliJ plugin `Save Actions`. Configure `Save Actions` in preferences to `Reformat file` on save.    
