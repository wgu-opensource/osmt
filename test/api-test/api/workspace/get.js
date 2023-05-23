// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();
console.log("Check user workspace");
pm.test("Check response type", function () {
  pm.expect(responseData.type).to.equal(expectedData.type);
});
pm.test("Check name exists", function () {
  pm.expect(responseData.name).exists;
});
pm.test("Check Id exists", function () {
  pm.expect(responseData.id).exists;
});
pm.test("Check owner exists", function () {
  pm.expect(responseData.owner).exists;
});
pm.test("Check description", function () {
  pm.expect(responseData.description).to.equal(expectedData.description);
});
pm.test("Check status", function () {
  pm.expect(responseData.status).to.equal(expectedData.status);
});
pm.test("Check skills", function () {
  pm.expect(responseData.skills).to.have.deep.equal(expectedData.skills);
});
pm.test("Check author exists", function () {
  pm.expect(responseData.author).exists;
});
pm.test("Check UUID exists", function () {
  pm.expect(responseData.uuid).exists;
});
pm.test("Check archive date does not exist", function () {
  pm.expect(responseData.archiveDate).not.exists;
});
pm.test("Check publish date does not exist", function () {
  pm.expect(responseData.publishDate).not.exists;
});
pm.test("Check creation date exists", function () {
  pm.expect(responseData.creationDate).exists;
});
pm.test("Check update date exists", function () {
  pm.expect(responseData.updateDate).exists;
});
pm.test("Check creator exists", function () {
  pm.expect(responseData.creator).exists;
});
pm.test("Check context", function () {
  pm.expect(responseData["@context"]).to.equal(expectedData["@context"]);
});