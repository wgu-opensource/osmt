pm.test("Check collections", function () {
  var expectedData = pm.environment.get("expectedResponse");
  var responseData = pm.response.json();

  pm.expect(responseData).to.have.deep.equal(expectedData);
});