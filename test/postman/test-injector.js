const fs = require('fs');
const path = require('path');

// const {chain} = require('stream-chain');
// const {parser} = require('stream-json/Parser');
// const {pick} = require('stream-json/filters/Pick');
// const {streamValues} = require('stream-json/streamers/StreamValues');
// const {stringer} = require('stream-json/Stringer');

const DEBUG = false;
const END = false;

// Files for read/write
const dataRoot = path.resolve(`${__dirname}/../api-test`);
let inputFile = `${__dirname}/osmt.postman_collection.json`;
// let inputFile = `${__dirname}/osmt-auth.postman_collection.json`;
let outputFile = `${__dirname}/osmt-testing.postman_collection.json`;

let exit = false;
let failedEndpoints = [];

// Check argument count
if (process.argv.length > 4) {
  argsError();
}
else if (process.argv.length == 3) {
  inputFile = path.resolve(process.argv[2]);
}
else if (process.argv.length == 4) {
  inputFile = path.resolve(process.argv[2]);
  outputFile = path.resolve(process.argv[3]);
}

// Requirements met
if (!exit) {
  main();
}


function main() {
  console.log(`Input: ${inputFile}`);
  console.log(`Output: ${outputFile}`);

  try {
    // let count = 0;

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

    for (let key in baseApi) {
      console.log(`${key}: ${baseApi[key]}`);
      if (key === "item") {
        console.log(`Data root: ${dataRoot}`);
        processRoutes(baseApi[key], dataRoot);
      }
    }

    fs.writeFileSync(outputFile, JSON.stringify(baseApi, null, '\t'));

  } catch (err) {
    console.error(err);
  }

  // Completion message
  if (failedEndpoints.length > 1) {
    console.log(`Could not parse test information for the following ${failedEndpoints.length} endpoints:`);
    console.log(failedEndpoints);
  }
  else {
    console.log("All endpoints successfully populated with tests.");
  }
}


// Improper arguments (files) specified
function argsError() {
  console.log("Usage: test-injector.js [input_json_file] [output_json_file]");
  exit = true;
}


/**
 * 
 * @param {*} array 
 * @param {*} path 
 */
function processRoutes(array, path) {
  if (DEBUG) {
    console.log(`Processing at '${path}'`);
  }

  // Process each item in current directory
  for (let item of array) {
    if (DEBUG) {
      console.log(`Subdirectory is present: ${item?.item != undefined}`);
    }

    // Subdirectory found
    if (item?.item) {
      let currentPath = `${path}/${item["name"]}`

      // Handle pre-request scripts
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

    if (END) {
      break;
    }
  }
}


/**
 * 
 * @param {*} item 
 * @param {*} path 
 */
function processEndpoint(item, path) {
  let requestType = item.request?.method.toLowerCase();

  // Handle pre-request scripts
  if (fs.existsSync(`${path}/${requestType}-pre-request.js`)) {
    processPreRequest(item, path, requestType);
  }

  // Retrieve API test information
  try {
    const scriptFile = `${requestType}.js`;
    const responseFile = `${requestType}.json`;
    
    // Read API test files
    const expectedResponse = fs.readFileSync(`${path}/${responseFile}`);
    const testScript = fs.readFileSync(`${path}/${scriptFile}`);

    // Build combined test script
    let combinedScript = `var expectedResponse = ${expectedResponse.toString()}`;
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
    failedEndpoints.push(path.replace(dataRoot, ''));
    console.error(err);
  }
}


/**
 * 
 * @param {*} item 
 * @param {*} path 
 */
function processPreRequest(item, path, requestType=null) {
  try {
    const scriptFile = `${requestType ? `${requestType}-` : ''}pre-request.js`;
    
    // Read API pre-request script
    let preRequestScript = fs.readFileSync(`${path}/${scriptFile}`);
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