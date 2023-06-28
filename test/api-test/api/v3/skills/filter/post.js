// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check resultant skills");

pm.test("Check skills count", function () {
  pm.expect(responseData.length).to.equal(expectedData.length);
});

for (let skillsIndex = 0; skillsIndex < expectedData.length; skillsIndex++) {
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
  pm.test(`Skill ${skillNum} - Check categories`, function () {
    pm.expect(responseSkill.categories).deep.equal(expectedSkill.categories);
  });
  pm.test(`Skill ${skillNum} - Check authors`, function () {
    pm.expect(responseSkill.authors).deep.equal(expectedSkill.authors);
  });
  pm.test(`Skill ${skillNum} - Check skill status`, function () {
    pm.expect(responseSkill.status).to.equal(expectedSkill.status);
  });
  pm.test(`Skill ${skillNum} - Check keywords exist`, function () {
    pm.expect(responseSkill.keywords).exists;
  });
  pm.test(`Skill ${skillNum} - Check occupations exist`, function () {
    pm.expect(responseSkill.occupations).exists;
  });
}