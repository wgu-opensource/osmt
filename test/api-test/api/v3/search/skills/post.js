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
  pm.test(`Skill ${skillNum} - Check skill name exists`, function () {
    pm.expect(responseLog.skillName).exists;
  });
  pm.test(`Skill ${skillNum} - Check skill statement exists`, function () {
    pm.expect(responseLog.skillStatement).exists;
  });
  pm.test(`Skill ${skillNum} - Check status exists`, function () {
    pm.expect(responseLog.status).exists;
  });
  pm.test(`Skill ${skillNum} - Check keywords exist`, function () {
    pm.expect(responseLog.keywords).exists;
  });
  pm.test(`Skill ${skillNum} - Check occupations exist`, function () {
    pm.expect(responseLog.occupations).exists;
  });
}