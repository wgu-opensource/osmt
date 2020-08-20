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

