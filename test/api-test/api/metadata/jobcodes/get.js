// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check job codes");

pm.test("Check job codes count", function () {
  pm.expect(expectedData.length).to.equal(50);
});

for (let jobCodeIndex = 0; jobCodeIndex < expectedData.length; jobCodeIndex++) {
  let expectedJobCode = expectedData[0];
  let jobCodeNum = jobCodeIndex + 1;

  let responseJobCode = expectedData[jobCodeIndex];
  pm.test(`JobCode ${jobCodeNum} - Check Id exists`, function () {
    pm.expect(responseJobCode.id).exists;
  });
  pm.test(`JobCode ${jobCodeNum} - Check code`, function () {
    pm.expect(responseJobCode.code).exists;
  });
  pm.test(`JobCode ${jobCodeNum} - Check targetNodeName`, function () {
    pm.expect(responseJobCode.targetNodeName).exists;
  });
  pm.test(`JobCode ${jobCodeNum} - Check frameworkName`, function () {
    pm.expect(responseJobCode.frameworkName).exists;
  });
  pm.test(`JobCode ${jobCodeNum} - Check level`, function () {
    pm.expect(responseJobCode.level).exists;
  });
  pm.test(`JobCode ${jobCodeNum} - Check jobCodeLevelAsNumber`, function () {
    pm.expect(responseJobCode.jobCodeLevelAsNumber).exists;
  });
  pm.test(`JobCode ${jobCodeNum} - Check parents`, function () {
    pm.expect(responseJobCode.parents).exists;
  });
}