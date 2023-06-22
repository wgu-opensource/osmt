// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check skills");

pm.test("Check skills count", function () {
  pm.expect(responseData.length > 0);
});

for (let skillsIndex = 0; skillsIndex < responseData.length; skillsIndex++) {
  let expectedSkill = expectedData[skillsIndex];
  let skillNum = skillsIndex + 1;

  let responseSkill = responseData[skillsIndex];
  pm.test(`Skill ${skillNum} - Check UUID exists`, function () {
    pm.expect(responseSkill.uuid).exists;
  });
  pm.test(`Skill ${skillNum} - Check Id exists`, function () {
    pm.expect(responseSkill.id).exists;
  });
  pm.test(`Skill ${skillNum} - Check skill name exists`, function () {
    pm.expect(responseSkill.skillName).exists;
  });
  pm.test(`Skill ${skillNum} - Check skill statement exists`, function () {
    pm.expect(responseSkill.skillStatement).exists;
  });
  pm.test(`Skill ${skillNum} - Check categories exist`, function () {
    pm.expect(responseSkill.categories).exists;
  });
  pm.test(`Skill ${skillNum} - Check authors exist`, function () {
    pm.expect(responseSkill.authors).exists;
  });
  pm.test(`Skill ${skillNum} - Check skill status exists`, function () {
    pm.expect(responseSkill.status).exists;
  });
  pm.test(`Skill ${skillNum} - Check keywords exist`, function () {
    pm.expect(responseSkill.keywords).exists;
  });
  pm.test(`Skill ${skillNum} - Check occupations exist`, function () {
    pm.expect(responseSkill.occupations).exists;
  });
}