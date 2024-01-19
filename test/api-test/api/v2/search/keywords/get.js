// let expectedData is injected from <response-type>.json
pm.test("Check keyword search", function () {
  let responseData = pm.response.json();
  // console.log(expectedData);
  // console.log(responseData);
  // pm.expect(responseData).to.have.deep.equal(expectedData);
  pm.expect(responseData.size).to.equal(expectedData.size)
});
