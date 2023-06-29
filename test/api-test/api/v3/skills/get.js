// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check skills");

pm.test("Check skills count", function () {
  pm.expect(responseData.length).to.equal(expectedData.length);
});

// FIXME add attributes
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
  pm.test(`Skill ${skillNum} - Check skill name`, function () {
    pm.expect(responseSkill.skillName).to.equal(expectedSkill.skillName);
  });
  pm.test(`Skill ${skillNum} - Check skill statement`, function () {
    pm.expect(responseSkill.statement).to.equal(expectedSkill.statement);
  });
  pm.test(`Skill ${skillNum} - Check skill status`, function () {
    pm.expect(responseSkill.status).to.equal(expectedSkill.status);
  });
  pm.test(`Skill ${skillNum} - Check keywords`, function () {
    pm.expect(responseSkill.keywords).to.have.deep.equal(expectedSkill.keywords);
  });
  pm.test(`Skill ${skillNum} - Check occupations`, function () {
    pm.expect(responseSkill.occupations).to.have.deep.equal(expectedSkill.occupations);
  });
}