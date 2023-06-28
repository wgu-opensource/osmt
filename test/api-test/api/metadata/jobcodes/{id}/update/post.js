// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();
console.log("Check updated job code");

pm.test("Check id exists", function() {
    pm.expect(responseData.id).to.equal(expectedData.id);
});
pm.test("Check code exists", function() {
    pm.expect(responseData.code).to.equal(expectedData.code);
});
pm.test("Check targetNodeName exists", function() {
    pm.expect(responseData.targetNodeName).to.equal(expectedData.targetNodeName);
});
pm.test("Check frameworkName exists", function() {
    pm.expect(responseData.frameworkName).to.equal(expectedData.frameworkName);
});
/* pm.test("Check jobCodeLevelAsNumber exists", function() {
    pm.expect(responseData.jobCodeLevelAsNumber).to.equal(expectedData.jobCodeLevelAsNumber);
}); */

