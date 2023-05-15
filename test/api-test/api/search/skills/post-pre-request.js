let uuidId = pm.request.url.variables.indexOf('collectionId');

if (uuidId > 0) {
  pm.request.url.query.remove('collectionId');
}

let search = {
  "query": "information"
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