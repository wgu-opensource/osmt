// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check a collection");

// FIXME - update these tests once docker images are cleaned up automatically
pm.test("Check type", function () {
  pm.expect(responseData.type).to.equal(expectedData.type);
});
pm.test("Check name", function () {
  pm.expect(responseData.name).exists;
});
pm.test("Check description", function () {
  pm.expect(responseData.description).exists;
});
pm.test("Check status", function () {
  pm.expect(responseData.status).exists;
});
pm.test("Check skills", function () {
  pm.expect(responseData.skills).exists;
});
pm.test("Check author", function () {
  pm.expect(responseData.author).exists;
});
pm.test("Check UUID", function () {
  pm.expect(responseData.uuid).exists;
});
pm.test("Check creation date exists", function () {
  pm.expect(responseData.creationDate).exists;
});
pm.test("Check Id exists", function () {
  pm.expect(responseData.id).exists;
});
pm.test("Check skill keywords", function () {
  pm.expect(responseData.skillKeywords).exists;
});
pm.test("Check context", function () {
  pm.expect(responseData["@context"]).exists;
});