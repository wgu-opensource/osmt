const fs = require('fs');
const glob = require('glob');
const path = require('path');

// TODO: convert to JSON streaming
// const {chain} = require('stream-chain');
// const {parser} = require('stream-json/Parser');
// const {pick} = require('stream-json/filters/Pick');
// const {streamValues} = require('stream-json/streamers/StreamValues');
// const {stringer} = require('stream-json/Stringer');

const DEBUG = false;
const END = false;

// Files for read/write
const dataRoot = path.resolve(`${__dirname}/../api-test`);
let inputFile = `${__dirname}/osmt-api-v3.postman_collection.json`;
let outputFile = `${__dirname}/osmt-testing-api-v3.postman_collection.json`;

// Processing trackers
let apiVersion = "v3";
let exit = false;
let availableData = {};
let failedEndpoints = [];

// Check argument count
if (process.argv.length > 5) {
  console.log("Usage: test-injector.js [input_json_file] [output_json_file] [api_version]");
  exit = true;
}
else if (process.argv.length == 3) {
  inputFile = path.resolve(process.argv[2]);
}
else if (process.argv.length >= 4) {
  inputFile = path.resolve(process.argv[2]);
  outputFile = path.resolve(process.argv[3]);
}
if (process.argv.length >= 5) {
  apiVersion = path.resolve(process.argv[4]);
}

// Requirements met
if (!exit) {
  main();
}

console.log("");


/**
 * Driver for injecting tests into api collection.
 */
function main() {
  console.log("Initializing test injection...")
  console.log(`Input: ${inputFile}`);
  console.log(`Output: ${outputFile}`);
  console.log("");

  try {
    // TODO: convert to JSON streaming
    // chain([
    //   fs.createReadStream(inputFile),
    //   parser(),
    //   // streamValues(),
    //   // data => JSON.stringify(data.value),
    //   pick({filter: 'data'}),
    //   stringer(),
    //   fs.createWriteStream(outputFile)
    // ]);

    // // pipeline.on('data', (data) => {
    // //   console.log(`${data}...`);
    // // });

    let baseApiRaw = fs.readFileSync(inputFile);
    let baseApi = JSON.parse(baseApiRaw);

    mapAvailableTestData(dataRoot, apiVersion);

    for (let key in baseApi) {
      if (DEBUG) {
        console.log(`${key}: ${baseApi[key]}`);
      }

      if (key === "item") {
        console.log("API structure found in input file. Parsing test data...");
        console.log(`Data root: ${dataRoot}`);
        console.log("");
        processRoutes(baseApi[key], dataRoot);
      }
    }

    fs.writeFileSync(outputFile, JSON.stringify(baseApi, null, '\t'));

  } catch (err) {
    throw err;
  }

  // Display errors for any missing test data
  if (failedEndpoints.length > 0) {
    console.log(`\nERROR: Could not parse test information for the following `
        + `${failedEndpoints.length} endpoint(s):`);
    console.log(failedEndpoints);
  }
  else {
    console.log("INFO: All collection endpoints successfully populated with tests.");
  }

  let unusedCount = 0;
  let unusedData = Object.keys(availableData).reduce((unusedData, key) => {
    if (availableData[key]) {
      unusedData[key.replace(dataRoot, '')] = availableData[key];
      unusedCount++;
    }
    return unusedData;
  }, {});

  // Display warnings for any unused test data
  if (unusedCount > 0) {
    console.log(`\nWARN: The following ${unusedCount} test data `
        + `files/directories are unused (consider deletion):`);
    console.log(unusedData);
  }

  // TODO return unused data and failed endpoints to shell script
  // possibly using throwing an exception
}


/**
 * Map available test directories and files.
 * 
 * @param {string} root root location of test data
 * @param {string} apiVersion api version (if present)
 */
function mapAvailableTestData(root, apiVersion=null) {
  root += "/api";

  if (apiVersion) {
    root += `/${apiVersion}`;
  }

  for (let file of glob.sync(root + '/**/*')) {
    availableData[file] = true;
  }
}


/**
 * Process defined API routes to find associated test data.
 * 
 * @param {Object[]} array  JSON object for API
 * @param {string} path     current route path
 */
function processRoutes(array, path) {
  if (DEBUG) {
    console.log(`Processing at '${path}'`);
  }
  availableData[path] = false;

  // Process each item in current directory
  for (let item of array) {
    if (DEBUG) {
      console.log(`Subdirectory is present: ${item?.item != undefined}`);
    }

    // Subdirectory found
    if (item?.item) {
      let currentPath = `${path}/${item["name"]}`

      // Handle directory-level pre-request scripts
      if (fs.existsSync(`${currentPath}/pre-request.js`)) {
        processPreRequest(item, currentPath);
      }

      processRoutes(item['item'], currentPath);
      if (DEBUG) {
        END = true;
      }
    }
    // Endpoint found
    else if (item?.request) {
      processEndpoint(item, path);
    }

    // Early exit for debug purposes
    if (END) {
      break;
    }
  }
}


/**
 * Process defined API endpoint to inject provided test data.
 * 
 * @param {Object} item JSON object for endpoint
 * @param {string} path current route path
 */
function processEndpoint(item, path) {
  let requestType = item.request?.method.toLowerCase();

  // Handle endpoint pre-request scripts
  if (fs.existsSync(`${path}/${requestType}-pre-request.js`)) {
    processPreRequest(item, path, requestType);
  }

  // Retrieve API test information
  try {
    const scriptFile = `${requestType}.js`;
    const scriptFileFull = `${path}/${scriptFile}`;
    const responseFile = `${requestType}.json`;
    const responseFileFull = `${path}/${responseFile}`;

    availableData[scriptFileFull] = false;
    availableData[responseFileFull] = false;
    
    // Read API test files
    const expectedData = fs.readFileSync(responseFileFull);
    const testScript = fs.readFileSync(scriptFileFull);

    // Build combined test script
    let combinedScript = `let expectedData = ${expectedData.toString()};`;
    combinedScript += '\n';
    combinedScript += testScript.toString();

    if (DEBUG) {
      console.log(combinedScript);
    }

    // Define events array for endpoint if not present
    if (!item?.event) {
      item['event'] = [];
    }

    // Define test event
    let testEvent = {
      'listen': 'test',
      'script': {
        'exec': combinedScript.split('\n'),
        'type': 'text/javascript'
      }
    };

    item.event.push(testEvent);
  } catch (err) {
    let failedEndpoint = `${path.replace(dataRoot, '')} : `
        + `${requestType.toUpperCase()}`;
    failedEndpoints.push(failedEndpoint);
    console.error(err);
  }
}


/**
 * Process pre-request script and inject as appropriate into collection.
 * 
 * @param {Object} item JSON object for API
 * @param {string} path current route path
 */
function processPreRequest(item, path, requestType=null) {
  try {
    const scriptFile = `${requestType ? `${requestType}-` : ''}pre-request.js`;
    const scriptFileFull = `${path}/${scriptFile}`;

    availableData[scriptFileFull] = false;

    // Read API pre-request script
    let preRequestScript = fs.readFileSync(scriptFileFull);
    preRequestScript = preRequestScript.toString();

    if (DEBUG) {
      console.log(preRequestScript);
    }

    // Define events array for endpoint if not present
    if (!item?.event) {
      item['event'] = [];
    }

    // Define test event
    let preRequestEvent = {
      'listen': 'prerequest',
      'script': {
        'exec': preRequestScript.split('\n'),
        'type': 'text/javascript'
      }
    };

    item.event.unshift(preRequestEvent);
  } catch (err) {
    console.error(err);
  }
}
