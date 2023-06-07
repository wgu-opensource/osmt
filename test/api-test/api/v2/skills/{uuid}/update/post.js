// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check resultant skill");

// FIXME - update these tests once docker images are cleaned up automatically
pm.test("Check type", function () {
  pm.expect(responseData.type).to.equal(expectedData.type);
});
pm.test("Check Id exists", function () {
  pm.expect(responseData.id).exists;
});
pm.test("Check creation date exists", function () {
  pm.expect(responseData.creationDate).exists;
});
pm.test("Check update date exists", function () {
  pm.expect(responseData.updateDate).exists;
});
pm.test("Check collections", function () {
  pm.expect(responseData.collections).exists;
});
pm.test("Check occupations", function () {
  pm.expect(responseData.occupations).exists;
});
pm.test("Check categories", function () {
  pm.expect(responseData.categories).exists;
});
pm.test("Check keywords", function () {
  pm.expect(responseData.keywords).exists;
});
pm.test("Check certifications", function () {
  pm.expect(responseData.certifications).exists;
});
pm.test("Check standards", function () {
  pm.expect(responseData.standards).exists;
});
pm.test("Check alignments", function () {
  pm.expect(responseData.alignments).exists;
});
pm.test("Check employers", function () {
  pm.expect(responseData.employers).exists;
});
pm.test("Check skill name", function () {
  pm.expect(responseData.skillName).to.equal(expectedData.skillName);
});
pm.test("Check skill statement", function () {
  pm.expect(responseData.skillStatement).to.equal(expectedData.skillStatement);
});
pm.test("Check status", function () {
  pm.expect(responseData.status).to.equal(expectedData.status);
});
pm.test("Check UUID", function () {
  pm.expect(responseData.UUID).to.equal(expectedData.UUID);
});
pm.test("Check authors", function () {
  pm.expect(responseData.authors).exists;
});
pm.test("Check creator exists", function () {
  pm.expect(responseData.creator).exists;
});
pm.test("Check context", function () {
  pm.expect(responseData["@context"]).to.equal(expectedData["@context"]);
});