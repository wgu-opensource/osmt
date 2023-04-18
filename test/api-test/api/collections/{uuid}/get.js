pm.test("Check a collection", function () {
  var expectedData = pm.environment.get("expectedResponse");
  var responseData = pm.response.json();

  pm.expect(responseData).to.have.deep.equal(expectedData);
});