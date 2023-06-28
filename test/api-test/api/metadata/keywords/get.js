// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check keywords");
console.log(responseData.length)
pm.test("Check keywords count", function () {
  pm.expect(responseData.length).to.equal(0);
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
  pm.test(`Keyword ${colNum} - Check status`, function () {
    pm.expect(responseCol.url).exists;
  });
}
