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
