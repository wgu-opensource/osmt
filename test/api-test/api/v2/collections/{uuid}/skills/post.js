// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check skills");

// FIXME - update these tests once docker images are cleaned up automatically
pm.test("Check skills count", function () {
  pm.expect(responseData.length > 0);
});

for (let skillsIndex = 0; skillsIndex < expectedData.length; skillsIndex++) {
  let expectedSkill = expectedData[0];
  let skillNum = skillsIndex + 1;

  let responseSkill = responseData[0];
  pm.test(`Skill ${skillNum} - Check UUID exists`, function () {
    pm.expect(responseSkill.uuid).exists;
  });
  pm.test(`Skill ${skillNum} - Check Id exists`, function () {
    pm.expect(responseSkill.id).exists;
  });
  pm.test(`Skill ${skillNum} - Check skill name`, function () {
    pm.expect(responseSkill.skillName).exists;
  });
  pm.test(`Skill ${skillNum} - Check skill statement`, function () {
    pm.expect(responseSkill.skillStatement).exists;
  });
  pm.test(`Skill ${skillNum} - Check category`, function () {
    pm.expect(responseSkill.category).exists;
  });
  pm.test(`Skill ${skillNum} - Check author`, function () {
    pm.expect(responseSkill.author).exists;
  });
  pm.test(`Skill ${skillNum} - Check skill status`, function () {
    pm.expect(responseSkill.status).exists;
  });
  pm.test(`Skill ${skillNum} - Check keywords`, function () {
    pm.expect(responseSkill.keywords).exists;
  });
  pm.test(`Skill ${skillNum} - Check occupations`, function () {
    pm.expect(responseSkill.occupations).exists;
  });
}
