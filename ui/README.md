# OSMT UI 

## IntelliJ setup

### Import module
  * Navigate to File -> New -> Module from existing sources
  * Select "Create module from existing sources"
  * Select the `./ui` folder where this README exists from the file dialog
  
### Run Configurations
  * Create a new `npm` configuration
  * Use package.json from the `./ui/package-json`
  * Set Scripts to `start-hotreload` (or `start` if not interested in live reloading)
  
---

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 10.0.6.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Whitelabel JSON config
A whitelabel JSON file's URI can be defined in the environment file for deployment.  This file is loaded dynamically at runtime and can be replaced 
without rebuilding with access to the deployed file.  The URI can be set for each angular environment by setting `environment.whiteLabelConfigUri`.  
On app launch, the file is downloaded in an api call and is deserialized into the AppConfig property `AppConfig.settings` which is available globally.

### How to update the shape of the config
If any changes to the signatures need to be made, such as adding new properties, then both the interface and class located in 
`src/app/models/app-config.model.ts` must be updated.
 
* The interface IAppConfig provides the expected shape of the json files and type safety when parsing config JSON. 
* The class DefaultAppConfig defines our default set of configurations when no overriding URI is provided.
    
## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
