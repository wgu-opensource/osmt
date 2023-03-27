/**
 * This file is a backup of the OSMT Postman API Collection's pre-request script.
 */

// Wrapper to chain async requests in Postman pre-request sandbox
const _dummy = setInterval(() => {}, 300000);
function sendRequest(req) {
    return new Promise((resolve, reject) => {
        pm.sendRequest(req, (err, res) => {
            return resolve(res);
        });
    });
};

(async function main() {
    // Per Okta endpoints documented at https://developer.okta.com/docs/reference/api/oidc

    // Get Okta Session Token for API access
    let authSessionUrl = pm.environment.get("oktaUrl") + "/api/v1/authn";
    console.log(`Get Okta Session Token...`);
    let result = await sendRequest({
        url: authSessionUrl,
        method: 'POST',
        header: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: {
            mode: 'raw',
            raw: JSON.stringify({
                username: `${pm.environment.get("oktaUsername")}`,
                password: `${pm.environment.get("oktaPassword")}`,
                options: {
                    multiOptionalFactorEnroll: false,
                    warnBeforePasswordExpired: true
                }
            })
        }
    });
    console.log(result.json());
    pm.environment.set("oktaSessionToken", result.json().sessionToken);

    // Get state information from OSMT app
    let authUrl = pm.environment.get("baseUrl") + "/oauth2/authorization/okta";
    console.log(`Obtain state information from OSMT app...`);
    result = await sendRequest({
        url: authUrl,
        method: 'GET',
        header: {
            'Accept': 'application/json',
        }
    });
    console.log("Information from OSMT app:");
    console.log(result);
    let authResponseHeaders = result.headers;
    let location = result.headers.get('Location');
    console.log(`location: ${location}`);

    // Obtain Okta authorization grant
    console.log(`Obtain Okta authorization grant...`);
    location = location + "&prompt=none" + "&sessionToken=" + pm.environment.get("oktaSessionToken");
    result = await sendRequest({
        url: location,
        method: 'GET',
        header: authResponseHeaders
    });
    console.log("Result from Okta authorization:");
    console.log(result);
    let grantResponseHeaders = result.headers;
    location = result.headers.get('Location');

    // Register access token (bearerToken) with OSMT Spring app using code value
    console.log(`Exchange Okta access token for OSMT bearer token...`);
    result = await sendRequest({
        url: location,
        method: 'GET',
        header: grantResponseHeaders
    });
    console.log("Result from OSMT token exchange:");
    console.log(result);
    location = result.headers.get('Location');
    parts = location.split('?');
    let osmtLink = parts[0];
    query = parts[1].split('&');
    parameters = {};
    query.forEach((entry) => {
        let p = entry.split('=');
        parameters[p[0]] = p[1];
    });
    let bearerToken = parameters.token;

    // Set the bearerToken for OSMT access
    console.log(`bearerToken: ${bearerToken}`);
    pm.environment.set("bearerToken", bearerToken);

    clearInterval(_dummy);
})();
