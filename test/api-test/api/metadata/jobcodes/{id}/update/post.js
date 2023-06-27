// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();
console.log("Check updated job code");

pm.test("Check id exists", function() {
    pm.expect(responseData.id).exists;
});
pm.test("Check code exists", function() {
    pm.expect(responseData.code).exists;
});
pm.test("Check targetNodeName exists", function() {
    pm.expect(responseData.targetNodeName).exists;
});
pm.test("Check frameworkName exists", function() {
    pm.expect(responseData.frameworkName).exists;
});
pm.test("Check jobCodeLevelAsNumber exists", function() {
    pm.expect(responseData.jobCodeLevelAsNumber).exists;
});

