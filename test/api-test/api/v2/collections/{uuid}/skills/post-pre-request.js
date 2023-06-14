let payload = {
  query: ""
};

let body = {
  mode: 'raw',
  raw: JSON.stringify(payload),
  options: {
    raw: {
      language: 'json'
    }
  }
};

pm.request.body.update(body);
