// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check skills search results");

pm.test("Check skills count", function () {
  pm.expect(responseData.length).to.equal(expectedData.length);
});

for (let skillIndex = 0; skillIndex < expectedData.length; skillIndex++) {
  let expectedLog = expectedData[skillIndex];
  let skillNum = skillIndex + 1;

  let responseLog = responseData[skillIndex];
  pm.test(`Skill ${skillNum} - Check UUID exists`, function () {
    pm.expect(responseLog.uuid).exists;
  });
  pm.test(`Skill ${skillNum} - Check Id exists`, function () {
    pm.expect(responseLog.id).exists;
  });
  pm.test(`Skill ${skillNum} - Check skill name`, function () {
    pm.expect(responseLog.skillName).to.equal(expectedLog.skillName);
  });
  pm.test(`Skill ${skillNum} - Check skill statement`, function () {
    pm.expect(responseLog.skillStatement).to.equal(expectedLog.skillStatement);
  });
  pm.test(`Skill ${skillNum} - Check status`, function () {
    pm.expect(responseLog.status).to.equal(expectedLog.status);
  });
  pm.test(`Skill ${skillNum} - Check keywords`, function () {
    pm.expect(responseLog.keywords).deep.equal(expectedLog.keywords);
  });
  pm.test(`Skill ${skillNum} - Check occupations`, function () {
    pm.expect(responseLog.occupations).deep.equal(expectedLog.occupations);
  });
}