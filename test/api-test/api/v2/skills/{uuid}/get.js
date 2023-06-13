// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check skill");

// FIXME - update/Add on for better test coverage
pm.test("Check type", function () {
    pm.expect(responseData.type).to.equal(expectedData.type);
});

pm.test("Check UUID", function () {
    pm.expect(responseData.uuid).exists;
});
