let sizeId = pm.request.url.query.indexOf('query');
if (sizeId < 0) {
  pm.request.url.query.add("query=NICE_SP");
}
else {
  pm.request.url.query.idx(sizeId).value = "NICE_SP";
}

let fromId = pm.request.url.query.indexOf('type');
if (fromId < 0) {
  pm.request.url.query.add("type=standard");
}
else {
  pm.request.url.query.idx(fromId).value = "standard";
}