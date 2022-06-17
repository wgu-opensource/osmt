(function (window) {
  var config = {
    production: false,
    env: 'local',

    baseApiUrl: "http://localhost:8080",
    clientId: "aa_osmt",
    clientIdHash: "YWFfb3NtdDp5JFF6Wjc4QXdXQXJDS2I0TENoSE9ZVFVTWmopKUV5I1NCRjY=",
    authUrl: "https://id.development.wgu.edu",
    redirectUrl: "http://localhost:4200/login/success",
    logoutUrl: 'https://id.development.wgu.edu/idp/startSLO.ping?redirect_url="https://localhost:4200"',

    dynamicWhitelabel: true
  };
  window.__env = window.__env || config;
})(this);
