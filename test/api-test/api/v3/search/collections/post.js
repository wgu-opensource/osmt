// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check collection search results");

pm.test("Check search result count", function () {
  pm.expect(responseData.length).to.equal(expectedData.length);
});

for (let collectionIndex = 0; collectionIndex < expectedData.length; collectionIndex++) {
  let expectedCol = expectedData[0];
  let colNum = collectionIndex + 1;

  let responseCol = responseData[0];
  pm.test(`Collection ${colNum} - Check UUID exists`, function () {
    pm.expect(responseCol.uuid).exists;
  });
  pm.test(`Collection ${colNum} - Check name`, function () {
    pm.expect(responseCol.name).to.equal(expectedCol.name);
  });
  pm.test(`Collection ${colNum} - Check description`, function () {
    pm.expect(responseCol.description).to.equal(expectedCol.description);
  });
  pm.test(`Collection ${colNum} - Check skill count`, function () {
    pm.expect(responseCol.skillCount).to.equal(expectedCol.skillCount);
  });
  pm.test(`Collection ${colNum} - Check status`, function () {
    pm.expect(responseCol.status).to.equal(expectedCol.status);
  });
}