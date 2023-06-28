// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check get All keywords");
pm.test("Check keywords count", function () {
  pm.expect(responseData.length).to.equal(expectedData.length);
});


for (let keywordIndex = 0; keywordIndex < responseData.length; keywordIndex++) {
  let colNum = keywordIndex + 1;

  let responseCol = responseData[keywordIndex];
  pm.test(`Keyword ${colNum} - Check Id exists`, function () {
    pm.expect(responseCol.id).exists;
  });
  pm.test(`Keyword ${colNum} - Check name`, function () {
    pm.expect(responseCol.name).exists;
  });
  pm.test(`Keyword ${colNum} - Check skill count`, function () {
    pm.expect(responseCol.skillCount).exists;
  });
}
