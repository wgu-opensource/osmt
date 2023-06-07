// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check skills search results");

// FIXME - update these tests once docker images are cleaned up automatically
pm.test("Check skills count", function () {
  pm.expect(responseData.length > 0);
});

for (let skillIndex = 0; skillIndex < expectedData.length; skillIndex++) {
  let expectedLog = expectedData[0];
  let skillNum = skillIndex + 1;

  let responseLog = responseData[0];
  pm.test(`Skill ${skillNum} - Check UUID exists`, function () {
    pm.expect(responseLog.uuid).exists;
  });
  pm.test(`Skill ${skillNum} - Check Id exists`, function () {
    pm.expect(responseLog.id).exists;
  });
  pm.test(`Skill ${skillNum} - Check skill name`, function () {
    pm.expect(responseLog.skillName).exists;
  });
  pm.test(`Skill ${skillNum} - Check skill statement`, function () {
    pm.expect(responseLog.skillStatement).exists;
  });
  pm.test(`Skill ${skillNum} - Check categories`, function () {
    pm.expect(responseLog.categories).exists;
  });
  pm.test(`Skill ${skillNum} - Check authors`, function () {
    pm.expect(responseLog.authors).exists;
  });
  pm.test(`Skill ${skillNum} - Check status`, function () {
    pm.expect(responseLog.status).exists;
  });
  pm.test(`Skill ${skillNum} - Check keywords`, function () {
    pm.expect(responseLog.keywords).exists;
  });
  pm.test(`Skill ${skillNum} - Check occupations`, function () {
    pm.expect(responseLog.occupations).exists;
  });
}