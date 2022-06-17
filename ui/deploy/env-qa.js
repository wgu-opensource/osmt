(function (window) {
  var config = {
    production: false,
    env: 'stage',

    baseApiUrl: "https://aa-skill.stage.wgu.edu",
    clientId: "aa_osmt",
    clientIdHash: "YWFfb3NtdDo4RTNYblNBKDQhTzN0bGw2Y244UiliQkkzN2deZEVXMEVqbjliKUl4QnJGNw==",
    authUrl: "https://id.stage.wgu.edu",
    redirectUrl: "https://osmt.stage.wgu.edu/login/success",
    logoutUrl: 'https://id.stage.wgu.edu/idp/startSLO.ping?redirect_url="https://osmt.stage.wgu.edu"',

    dynamicWhitelabel: true
  };
  window.__env = window.__env || config;
})(this);
