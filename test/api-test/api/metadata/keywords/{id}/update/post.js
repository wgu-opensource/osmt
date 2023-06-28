// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();
console.log("Check a keyword update");

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
