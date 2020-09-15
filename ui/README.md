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
The default config file is located in `src/assets/config.json`.  This file is loaded dynamically at runtime and can be 
replaced without rebuilding with access to the deployed file.

The uri of the configuration can be set for each angular environment by setting `environment.whiteLabelConfigUri` to any uri other 
than the default location.  At app launch, the file is downloaded in an api call and is deserialized into the AppConfig property `AppConfig.settings` which is available globally.

### How to update the shape of the config
If any changes to the signatures need to be made.  Such as adding new properties, then the interface used to type-check the configuration must be updated.
 
The interface IAppConfig provides the expected shape of the json files.  Modify this interface, then ensure that all json config files that may be loaded are similarly updated.
If the file can't be deserialized into an implementation of IAppConfig, the app should fail at launch.
    
## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
