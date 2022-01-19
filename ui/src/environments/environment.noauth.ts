/*
  This is for local development while using the 'noauth' Spring profile. You can start the
  webpack server with the local Angular CLI using this command:
  npm start -- --configuration=noauth
*/

export const environment = {
  production: false,
  baseApiUrl: "http://localhost:8080",
  // this value will not be used
  loginUrl: "http://localhost:8080/oauth2/authorization/okta",
  dynamicWhitelabel: true,
  isAuthEnabled: false
}
