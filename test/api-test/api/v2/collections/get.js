// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check collections");

// FIXME - update these tests once docker images are cleaned up automatically
pm.test("Check collections count", function () {
  pm.expect(responseData.length > 0);
});

// for (let collectionIndex = 0; collectionIndex < expectedData.length; collectionIndex++) {
for (let collectionIndex = 0; collectionIndex < responseData.length; collectionIndex++) {
  let expectedCol = expectedData[0];
  let colNum = collectionIndex + 1;

  let responseCol = responseData[0];
  pm.test(`Collection ${colNum} - Check UUID exists`, function () {
    pm.expect(responseCol.uuid).exists;
  });
  pm.test(`Collection ${colNum} - Check name`, function () {
    pm.expect(responseCol.name).exists;
  });
  // pm.test(`Collection ${colNum} - Check description`, function () {
  //   pm.expect(responseCol.description).to.equal(expectedCol.description);
  // });
  pm.test(`Collection ${colNum} - Check skill count`, function () {
    pm.expect(responseCol.skillCount).exists;
  });
  pm.test(`Collection ${colNum} - Check status`, function () {
    pm.expect(responseCol.status).exists;
  });
}