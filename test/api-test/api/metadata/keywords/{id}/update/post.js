// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check a keyword update");

// FIXME - update these tests once docker images are cleaned up automatically

pm.test(`Keyword - Check object type`, function () {
  pm.expect(responseData.type).to.equal(expected.type);
});
pm.test(`Keyword - Check name`, function () {
  pm.expect(responseData.name).to.equal(expected.name);
});
pm.test(`Keyword - Check ID exists`, function () {
  pm.expect(responseData.id).exists;
});
pm.test(`Keyword - Check framework`, function () {
  pm.expect(responseData.framework).to.equal(expected.framework);
});
pm.test(`Keyword - Check Url`, function () {
  pm.expect(responseCol.url).to.equal(expected.url);
});
