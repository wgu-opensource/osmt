// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check collection creation");

pm.test("Check collection count", function () {
  pm.expect(responseData.length).to.equal(expectedData.length);
});

for (let collectionIndex = 0; collectionIndex < expectedData.length; collectionIndex++) {
  let expectedCol = expectedData[0];
  let colNum = collectionIndex + 1;

  let responseCol = responseData[0];
  pm.test(`Collection ${colNum} - Check object type`, function () {
    pm.expect(responseCol.type).to.equal(expectedCol.type);
  });
  pm.test(`Collection ${colNum} - Check collection name`, function () {
    pm.expect(responseCol.name).to.equal(expectedCol.name);
  });
  pm.test(`Collection ${colNum} - Check ID exists`, function () {
    pm.expect(responseCol.id).exists;
  });
  pm.test(`Collection ${colNum} - Check description`, function () {
    pm.expect(responseCol.description).to.equal(expectedCol.description);
  });
  pm.test(`Collection ${colNum} - Check status`, function () {
    pm.expect(responseCol.status).to.equal(expectedCol.status);
  });
  pm.test(`Collection ${colNum} - Check skills`, function () {
    pm.expect(responseCol.skills).to.have.deep.equal(expectedCol.skills);
  });
  pm.test(`Collection ${colNum} - Check author`, function () {
    pm.expect(responseCol.author).to.equal(expectedCol.author);
  });
  pm.test(`Collection ${colNum} - Check UUID exists`, function () {
    pm.expect(responseCol.uuid).exists;
  });
  pm.test(`Collection ${colNum} - Check archive date does not exist`, function () {
    pm.expect(responseCol.archiveDate).not.exists;
  });
  pm.test(`Collection ${colNum} - Check publish date does not exist`, function () {
    pm.expect(responseCol.publishDate).not.exists;
  });
  pm.test(`Collection ${colNum} - Check creation date exists`, function () {
    pm.expect(responseCol.creationDate).exists;
  });
  pm.test(`Collection ${colNum} - Check update date exists`, function () {
    pm.expect(responseCol.updateDate).exists;
  });
  pm.test(`Collection ${colNum} - Check creator exists`, function () {
    pm.expect(responseCol.creator).exists;
  });
  pm.test(`Collection ${colNum} - Check context`, function () {
    pm.expect(responseCol["@context"]).to.equal(expectedCol["@context"]);
  });
}
