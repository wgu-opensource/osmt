import { writeFile } from 'fs';

// use env provided by osmt.env in project root
require('dotenv').config( { path: '../osmt.env' } );

// environment.prod.ts is set in project .gitignore file
const targetPath = './src/environments/environment.prod.ts';

const envConfigFile = `export const environment = {
  production: true,
  baseApiUrl: "${process.env.OSMT_API_URL || ''}",
  loginUrl: "${process.env.OSMT_LOGIN_URL || '/oauth2/authorization/okta'}",
  dynamicWhitelabel: ${process.env.OSMT_DYNAMIC_WHITELABEL !== "false" && (process.env.OSMT_WHITELABEL_URL && process.env.OSMT_WHITELABEL_URL.length > 0) ? true : false},
  whiteLabelUrl: "${process.env.OSMT_WHITELABEL_URL || '/whitelabel/whitelabel.json'}"
};
`;


writeFile(targetPath, envConfigFile, function (err) {
  if (err) {
    throw console.error(err);
  } else {
    console.log(`Angular environment.ts file generated correctly at ${targetPath}\n${envConfigFile}`);
  }
});
