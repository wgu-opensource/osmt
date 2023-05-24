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

let queryId = pm.request.url.query.indexOf('query')