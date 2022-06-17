// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: false,
  baseApiUrl: "http://localhost:8080",
  clientId: "aa_osmt",
  clientIdHash: "YWFfb3NtdDooMmp5d1owRW5DQ1BENlhyNEZeQ3NvYTdMR0VIMk5U",
  authUrl: "https://id.sbx.wgu.edu",
  redirectUrl: "http://localhost:4200/login/success",
  logoutUrl: "https://id.sbx.wgu.edu/idp/startSLO.ping?redirect_url=\"https://localhost:4200\"",

  dynamicWhitelabel: true
}

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.
