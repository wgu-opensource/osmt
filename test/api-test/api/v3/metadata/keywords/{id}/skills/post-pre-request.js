let id = pm.request.url.variables.indexOf('id');
pm.request.url.variables.idx(id).value = "1382";

let sizeId = pm.request.url.query.indexOf('size');
if (sizeId < 0) {
    pm.request.url.query.add("size=50");
}
else {
    pm.request.url.query.idx(sizeId).value = "50";
}

let fromId = pm.request.url.query.indexOf('from');
if (fromId < 0) {
    pm.request.url.query.add("from=0");
}
else {
    pm.request.url.query.idx(fromId).value = "0";
}

let sortId = pm.request.url.query.indexOf('sort');
if (sortId < 0) {
    pm.request.url.query.add("sort=name.asc");
}
else {
    pm.request.url.query.idx(sortId).value = "name.asc";
}

let statusId = pm.request.url.query.indexOf('status');
if (statusId > 0) {
  pm.request.url.removeQueryParams('status');
}

pm.request.url.query.add("status=draft");
pm.request.url.query.add("status=published");
pm.request.url.query.add("status=archived");
pm.request.url.query.add("status=deleted")

let apiSearch = {
  "query": ""
};

let body = {
  mode: 'raw',
  raw: JSON.stringify(apiSearch),
  options: {
    raw: {
      language: 'json'
    }
  }
};

pm.request.body.update(body);
