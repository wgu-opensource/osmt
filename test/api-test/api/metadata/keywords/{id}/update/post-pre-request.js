let updates = {
  "framework": "framework1"
};

let body = {
  mode: 'raw',
  raw: JSON.stringify(updates),
  options: {
    raw: {
      language: 'json'
    }
  }
};

pm.request.body.update(body);