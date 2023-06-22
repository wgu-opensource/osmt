// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check a collection");

pm.test("Check type", function () {
  pm.expect(responseData.type).to.equal(expectedData.type);
});
pm.test("Check name", function () {
  pm.expect(responseData.name).to.equal(expectedData.name);
});
pm.test("Check description", function () {
  pm.expect(responseData.description).to.equal(expectedData.description);
});
pm.test("Check status", function () {
  pm.expect(responseData.status).to.equal(expectedData.status);
});
pm.test("Check skills", function () {
  pm.expect(responseData.skills).deep.equal(expectedData.skills);
});
pm.test("Check author", function () {
  pm.expect(responseData.author).to.equal(expectedData.author);
});
pm.test("Check UUID", function () {
  pm.expect(responseData.uuid).to.equal(expectedData.uuid);
});
pm.test("Check creation date exists", function () {
  pm.expect(responseData.creationDate).exists;
});
pm.test("Check Id exists", function () {
  pm.expect(responseData.id).exists;
});
pm.test("Check skill keywords", function () {
  pm.expect(responseData.skillKeywords).deep.equal(expectedData.skillKeywords);
});
pm.test("Check context", function () {
  pm.expect(responseData["@context"]).to.equal(expectedData["@context"]);
});