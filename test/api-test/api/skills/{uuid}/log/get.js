// var expectedData is injected from <response-type>.json

ppm.test("Check RSD log", function () {
    var responseData = pm.response.json();

    pm.expect(responseData).to.have.deep.equal(expectedData);
});