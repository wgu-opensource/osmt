// var expectedData is injected from <response-type>.json

ppm.test("Check skills", function () {
  var responseData = pm.response.json();

  pm.expect(responseData).to.have.deep.equal(expectedData);
});