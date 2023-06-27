// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check job codes");

pm.test("Check job codes count", function () {
  pm.expect(responseData.length).to.equal(expectedData.length);
});

for (let jobCodeIndex = 0; jobCodeIndex < expectedData.length; jobCodeIndex++) {
  let expectedJobCode = expectedData[jobCodeIndex];
  let jobCodeNum = jobCodeIndex + 1;

  let responseJobCode = responseData[jobCodeIndex];
  pm.test(`JobCode ${jobCodeNum} - Check Id exists`, function () {
    pm.expect(responseJobCode.id).exists;
  });
  pm.test(`JobCode ${jobCodeNum} - Check code`, function () {
    pm.expect(responseJobCode.code).to.equal(expectedJobCode.code);
  });
  pm.test(`JobCode ${jobCodeNum} - Check targetNodeName`, function () {
    pm.expect(responseJobCode.targetNodeName).to.equal(expectedJobCode.targetNodeName);
  });
  pm.test(`JobCode ${jobCodeNum} - Check frameworkName`, function () {
    pm.expect(responseJobCode.frameworkName).to.equal(expectedJobCode.frameworkName);
  });
  pm.test(`JobCode ${jobCodeNum} - Check level`, function () {
    pm.expect(responseJobCode.level).to.equal(expectedJobCode.level);
  });
  pm.test(`JobCode ${jobCodeNum} - Check jobCodeLevelAsNumber`, function () {
    pm.expect(responseJobCode.jobCodeLevelAsNumber).to.equal(expectedJobCode.jobCodeLevelAsNumber);
  });
  pm.test(`JobCode ${jobCodeNum} - Check parents`, function () {
    pm.expect(responseJobCode.parents).to.have.deep.equal(expectedJobCode.parents);
  });
}