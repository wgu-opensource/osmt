const authEnv = require('./osmt-auth.environment.json');

(function getToken() {
    return console.log(authEnv.values[10].value)
})()

