let sortId = pm.request.url.query.indexOf('sort');
if (sortId < 0) {
  pm.request.url.query.add("sort=name.asc");
}
else {
  pm.request.url.query.idx(sortId).value = "name.asc";
}

let search = {
  "advanced": {
    "collectionName": "ma",
    "author": "OSMT Developer"
  }
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