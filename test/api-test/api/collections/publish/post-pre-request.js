let collections = {
  "uuids": [
    "187db436-d3e7-43c7-980c-83e143f3df7f"
  ]
};

let body = {
  mode: 'raw',
  raw: JSON.stringify(collections),
  options: {
    raw: {
      language: 'json'
    }
  }
};

pm.request.body.update(body);