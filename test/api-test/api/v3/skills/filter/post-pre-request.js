let sizeId = pm.request.url.query.indexOf('size');
if (sizeId < 0) {
  pm.request.url.query.add("size=50");
}
else {
  pm.request.url.query.idx(sizeId).value = "50";
}

let fromId = pm.request.url.query.indexOf('from');
if (fromId < 0) {
  pm.request.url.query.add("size=0");
}
else {
  pm.request.url.query.idx(fromId).value = "0";
}

let statusId = pm.request.url.query.indexOf('status');
if (statusId > 0) {
  pm.request.url.removeQueryParams('status');
}

pm.request.url.query.add("status=Published");

let filter = {
  "filtered": {
    "keywords": [
      "Human Resource Management"
    ],
    "occupations": [
      "17-2000"
    ],
    "authors": [
      "Western Governors University"
    ]
  }
};

let body = {
  mode: 'raw',
  raw: JSON.stringify(filter),
  options: {
    raw: {
      language: 'json'
    }
  }
};

pm.request.body.update(body);