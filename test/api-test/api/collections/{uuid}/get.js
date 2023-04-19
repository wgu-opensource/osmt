// var expectedData is injected from <response-type>.json

pm.test("Check a collection", function () {
  var responseData = pm.response.json();

  pm.expect(responseData).to.have.deep.equal(expectedData);
});