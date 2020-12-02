// karma.conf.js
module.exports = function(config) {
    config.set({
      frameworks: ['jasmine', '@angular-devkit/build-angular'],
      plugins: [
        require('karma-jasmine'),
        require('karma-chrome-launcher'),
        require('karma-jasmine-html-reporter'),
        require('karma-coverage-istanbul-reporter'),
        require('karma-junit-reporter'),
        require('@angular-devkit/build-angular/plugins/karma')
      ],
      files: ['src/app/**/*.spec.ts'],
      reporters: ['progress', 'junit'],
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
      junitReporter: {
        outputDir: 'test-results/', // results will be saved as $outputDir/$browserName.xml
        outputFile: undefined, // if included, results will be saved as $outputDir/$browserName/$outputFile
        suite: 'client', // suite will become the package name attribute in xml testsuite element
        useBrowserName: true, // add browser name to report and classes names
        nameFormatter: undefined, // function (browser, result) to customize the name attribute in xml testcase element
        classNameFormatter: undefined, // function (browser, result) to customize the classname attribute in xml testcase element
        properties: {}, // key value pair of properties to add to the <properties> section of the report
        xmlVersion: null // use '1' if reporting to be per SonarQube 6.2 XML format
      },
      autoWatch: false,
      singleRun: true, // Karma captures browsers, runs the tests and exits
      concurrency: Infinity
    })
  }
  