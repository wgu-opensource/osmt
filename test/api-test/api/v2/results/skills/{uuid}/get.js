// let expectedData is injected from <response-type>.json

pm.test("Check skill creation task result", function () {
  let responseData = pm.response.text();
  console.log(responseData);

  pm.expect(responseData).to.equal(expectedData);
});