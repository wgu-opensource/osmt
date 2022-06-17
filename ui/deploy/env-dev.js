(function (window) {
  var config = {
    production: false,
    env: 'development',

    baseApiUrl: "https://aa-skill.development.wgu.edu",
    clientId: "aa_osmt",
    clientIdHash: "YWFfb3NtdDp5JFF6Wjc4QXdXQXJDS2I0TENoSE9ZVFVTWmopKUV5I1NCRjY=",
    authUrl: "https://id.development.wgu.edu",
    redirectUrl: "https://osmt.development.wgu.edu/login/success",
    logoutUrl: 'https://id.development.wgu.edu/idp/startSLO.ping?redirect_url="https://osmt.development.wgu.edu"',

    dynamicWhitelabel: true
  };
  window.__env = window.__env || config;
})(this);
