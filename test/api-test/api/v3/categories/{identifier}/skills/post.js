let search = {
  "filtered": {}
};

let body = {
  mode: 'raw',
  raw: JSON.stringify(search),
  options: {
    raw: {
      language: 'json'
    }
  }
};

pm.request.body.update(body);