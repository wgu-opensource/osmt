// var expectedData is injected from <response-type>.json

pm.test("Check user workspace", function () {
  var responseData = pm.response.json();

  // pm.expect(responseData).to.have.deep.equal(expectedData);
  pm.expect(responseData.type).to.equal(expectedData.type);
  pm.expect(responseData.name).exists;
  pm.expect(responseData.id).exists;
  pm.expect(responseData.owner).exists;
  pm.expect(responseData.description).to.equal(expectedData.description);
  pm.expect(responseData.status).to.equal(expectedData.status);
  pm.expect(responseData.skills).to.equal(expectedData.skills);
  pm.expect(responseData.author).exists;
  pm.expect(responseData.uuid).exists;
  pm.expect(responseData.archiveDate).to.equal(expectedData.archiveDate);
  pm.expect(responseData.publishDate).to.equal(expectedData.publishDate);
  pm.expect(responseData.creationDate).exists;
  pm.expect(responseData.updateDate).exists;
  pm.expect(responseData.creator).exists;
  pm.expect(responseData["@context"]).to.equal(expectedData["@context"]);
});