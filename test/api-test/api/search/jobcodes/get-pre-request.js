var sizeId = pm.request.url.query.indexOf('query');
if (sizeId < 0) {
  pm.request.url.query.add("query=15-121");
}
else {
  pm.request.url.query.idx(sizeId).value = "15-121";
}