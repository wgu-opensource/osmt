// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check collections");

pm.test("Check collections count", function () {
  pm.expect(responseData.length > 0);
});

for (let collectionIndex = 0; collectionIndex < responseData.length; collectionIndex++) {
  let expectedCol = expectedData[collectionIndex];
  let colNum = collectionIndex + 1;

  let responseCol = responseData[collectionIndex];
  pm.test(`Collection ${colNum} - Check UUID exists`, function () {
    pm.expect(responseCol.uuid).exists;
  });
  pm.test(`Collection ${colNum} - Check name exists`, function () {
    pm.expect(responseCol.name).exists;
  });
  pm.test(`Collection ${colNum} - Check skill count existws`, function () {
    pm.expect(responseCol.skillCount).exists;
  });
  pm.test(`Collection ${colNum} - Check status`, function () {
    pm.expect(responseCol.status).to.equal(expectedCol.status);
  });
}