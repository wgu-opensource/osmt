(function (window) {
  var config = {
    production: false,
    env: 'sbx',

    baseApiUrl: "https://aa-skill.sbx.wgu.edu",
    clientId: "aa_osmt",
    clientIdHash: "YWFfb3NtdDp5JFF6Wjc4QXdXQXJDS2I0TENoSE9ZVFVTWmopKUV5I1NCRjY=",
    authUrl: "https://id.development.wgu.edu",
    redirectUrl: "https://osmt.sbx.wgu.edu/login/success",
    logoutUrl: 'https://id.development.wgu.edu/idp/startSLO.ping?redirect_url="https://osmt.sbx.wgu.edu"',

    dynamicWhitelabel: true
  };
  window.__env = window.__env || config;
})(this);
