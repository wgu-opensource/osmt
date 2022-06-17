# OSMT UI 
The OSMT UI is an Angular application and will be responsible for the following tasks:
- managing the definition of skills
- managing the organization of skills through collections

## API specification
[API spec](https://github.com/WGU-edu/ngp-aa-osmt/blob/sandbox/doc/arch/int/osmt-v1.0.x-openapi.yaml)

# Contacts
The following are the engineers on Team Diamond.  Please **only** use the contact info to contact the **on-call** individual.

Engineer | Cell phone
--- | ---
Stirling Hale | +1-435-256-1738
Cam Peterson | +1-503-332-2270
Huey Nguyen | +1-916-752-2229
Jesús Baustista Gudino | +52-33-1072-8609

# Release Notes
## 1.0.4-8
Release Date: May 10, 2022
* DMND-950 - Fixed Add RSDs to Collections

## 1.0.4-7
Release Date: Mar 18, 2022.
* DMND-966 - Programmatically retrieve JSON from canonical URL.
* DMND-943 - Implemented "loading" page while loading information.

## 1.0.4-6
Release Date: Feb 24, 2022.
  * DMND-821: Limit OSMT UI users (by role) to just the Skills Architects.
  * DMND-937: Apply the default author when batch import process have the author field empty.
  * Added more tests.

## 1.0.4-5
Release Date: Jan 28, 2022.
  * DMND-569: Application now uses logout/login page correctly.
  * DMND-925: Adding versions.json generation so that aa-monitor can detect the version of what is running locally.
  * DMND-849: Updated the baseApiUrl to use always 'osmt.wgu.edu' (without the .prod in it).
  * Added more tests.

## 1.0.4-4

Release Date: Aug 10, 2021.
  * DMND-701: Default author wasn't being applied.
  * DMND-704: Detect and prevent duplicate records.
  * Added more tests.

## 1.0.4-3

Release Date: Jul 28, 2021
  * DMND-698: Now displaying "+" symbol when total RSDs >= 10000.

## 1.0.4-2

Release Date: Jul 16, 2021
  * DMND-669: Getting the displaying occupations name in the correct way.
  
## 1.0.4-1

Release Date: Jul 16, 2021
  * DMND-670: UI now reads whitelabel.json from it's own webserver.

## 1.0.4-0

Release Date: Jun 29, 2021
  * Initial Release

## IntelliJ setup

### Import module
  * Navigate to File -> New -> Module from existing sources
  * Select "Create module from existing sources"
  * Select the `./` folder where this README exists from the file dialog
  
### Run Configurations
  * Create a new `npm` configuration
  * Use package.json from the `./package-json`
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
