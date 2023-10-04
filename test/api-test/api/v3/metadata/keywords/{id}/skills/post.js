// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check get related RSDs");

pm.test("Check keywords count", function () {
  pm.expect(responseData.length).to.equal(expectedData.length);
});


for (let rsdIndex = 0; rsdIndex < responseData.length; rsdIndex++) {
  let colNum = rsdIndex + 1;

  let responseCol = responseData[rsdIndex];
  let expectedCol = expectedData[rsdIndex];

  pm.test("RSD in expected data is equal to responde data", function() {
    pm.expect(responseCol).to.have.deep.equal(expectedCol)
  });
}
