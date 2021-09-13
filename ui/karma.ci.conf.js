'use strict';

var path = require('path');

module.exports = function(config) {
    config.set({
      frameworks: ['jasmine', '@angular-devkit/build-angular'],
      plugins: [
        require('karma-jasmine'),
        require('karma-chrome-launcher'),
        require('karma-jasmine-html-reporter'),
        require('karma-coverage'),
        require('karma-sonarqube-unit-reporter'),
        require('@angular-devkit/build-angular/plugins/karma')
      ],
      files: ['src/app/**/*.spec.ts'],
      reporters: ['progress', 'sonarqubeUnit', 'coverage'],
      port: 9876,  // karma web server port
      colors: true,
      logLevel: config.LOG_INFO,
      browsers: ['ChromeHeadlessNoSandbox'],
      customLaunchers: {
        ChromeHeadlessNoSandbox: {
          base: 'ChromeHeadless',
          flags: ['--no-sandbox']
        }
      },
      coverageReporter: {
        reporters: [
          {
            type: 'lcov',
            dir: 'reports',
            subdir: 'coverage',
          },
          {
            type: 'text-summary'
          },
        ]
      },
      sonarQubeUnitReporter: {
        sonarQubeVersion: 'LATEST',
        outputFile: 'reports/ut_report.xml',
        useBrowserName: false
      },
      autoWatch: false,
      singleRun: true, // Karma captures browsers, runs the tests and exits
      concurrency: Infinity
    })
  }
