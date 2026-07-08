// This file is required by karma.conf.js and loads recursively all the .spec and framework files

import './app/app.module'; // Force Karma to load all source files, not just those that have a corresponding spec file
import 'zone.js/testing';
import { getTestBed, TestBed } from '@angular/core/testing';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting,
} from '@angular/platform-browser-dynamic/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';

// First, initialize the Angular testing environment.
getTestBed().initTestEnvironment(
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting(),
  {
    teardown: { destroyAfterEach: false },
  }
);

// Patch TestBed.configureTestingModule to automatically add NO_ERRORS_SCHEMA
// This suppresses warnings about unknown elements and properties in component templates during tests
const originalConfigureTestingModule = TestBed.configureTestingModule;
TestBed.configureTestingModule = function (moduleDef: any) {
  if (!moduleDef.schemas) {
    moduleDef.schemas = [NO_ERRORS_SCHEMA];
  } else if (!moduleDef.schemas.includes(NO_ERRORS_SCHEMA)) {
    moduleDef.schemas.push(NO_ERRORS_SCHEMA);
  }
  return originalConfigureTestingModule.call(this, moduleDef);
};
