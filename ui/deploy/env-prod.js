(function (window) {
  var config = {
    production: false,
    env: 'production',

    baseApiUrl: "https://aa-skill.wgu.edu",
    clientId: "aa_osmt",
    clientIdHash: "YWFfb3NtdDo3VWJGMG9rRCQkYzlDNSRwKkM0RmdENSpiZlpxZSpaRE9tQTE4TyppbG5LZlA=",
    authUrl: "https://id.prod.wgu.edu",
    redirectUrl: "https://osmt.prod.wgu.edu/login/success",
    logoutUrl: 'https://id.prod.wgu.edu/idp/startSLO.ping?redirect_url="https://osmt.prod.wgu.edu"',

    dynamicWhitelabel: true
  };
  window.__env = window.__env || config;
})(this);
