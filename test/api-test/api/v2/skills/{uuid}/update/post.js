// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check resultant skill");

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
  pm.expect(responseData.collections).deep.equal(expectedData.collections);
});
pm.test("Check occupations", function () {
  pm.expect(responseData.occupations).deep.equal(expectedData.occupations);
});
pm.test("Check categories", function () {
  pm.expect(responseData.categories).deep.equal(expectedData.categories);
});
pm.test("Check keywords", function () {
  pm.expect(responseData.keywords).deep.equal(expectedData.keywords);
});
pm.test("Check certifications", function () {
  pm.expect(responseData.certifications).deep.equal(expectedData.certifications);
});
pm.test("Check standards", function () {
  pm.expect(responseData.standards).deep.equal(expectedData.standards);
});
pm.test("Check alignments", function () {
  pm.expect(responseData.alignments).deep.equal(expectedData.alignments);
});
pm.test("Check employers", function () {
  pm.expect(responseData.employers).deep.equal(expectedData.employers);
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
  pm.expect(responseData.authors).deep.equal(expectedData.authors);
});
pm.test("Check creator exists", function () {
  pm.expect(responseData.creator).exists;
});
pm.test("Check context", function () {
  pm.expect(responseData["@context"]).to.equal(expectedData["@context"]);
});