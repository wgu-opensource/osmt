let skills = {
  "uuids": [
    "a5e7fd85-5b0a-4186-937b-62b9123fc7ae",
    "ff80707d-2b55-4673-99cf-8265d74053a1"
  ]
};

let body = {
  mode: 'raw',
  raw: JSON.stringify(skills),
  options: {
    raw: {
      language: 'json'
    }
  }
};

pm.request.body.update(body);