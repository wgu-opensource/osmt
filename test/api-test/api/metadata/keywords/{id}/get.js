// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check a keyword retrieval");

pm.test("Check type", function () {
  pm.expect(responseData.type).to.equal(expectedData.type);
});
pm.test("Check name", function () {
  pm.expect(responseData.name).exists;
});
pm.test("Check framework", function () {
  pm.expect(responseData.framework).exists;
});
pm.test("Check url", function () {
  pm.expect(responseData.url).exists;
});
pm.test("Check skillCount", function () {
  pm.expect(responseData.skillCount).exists;
});
pm.test("Check ID", function () {
  pm.expect(responseData.id).exists;
});
