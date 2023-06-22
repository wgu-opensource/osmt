// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check skill");

pm.test(`Check UUID exists`, function () {
  pm.expect(responseData.uuid).exists;
});
pm.test(`Check Id exists`, function () {
  pm.expect(responseData.id).exists;
});
pm.test(`Check skill name`, function () {
  pm.expect(responseData.skillName).to.equal(expectedData.skillName);
});
pm.test(`Check skill statement`, function () {
  pm.expect(responseData.statement).to.equal(expectedData.statement);
});
pm.test(`Check skill status`, function () {
  pm.expect(responseData.status).to.equal(expectedData.status);
});
pm.test(`Check keywords`, function () {
  pm.expect(responseData.keywords).to.have.deep.equal(expectedData.keywords);
});
pm.test(`Check occupations`, function () {
  pm.expect(responseData.occupations).to.have.deep.equal(expectedData.occupations);
});