// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();
console.log("Check category");

pm.test("Check value exists", function() {
    pm.expect(responseData.value).exists;
});
pm.test("Check id exists", function() {
    pm.expect(responseData.id).exists;
});
pm.test("Check type exists", function() {
    pm.expect(responseData.type).exists;
});
pm.test("Check skillCount exists", function() {
    pm.expect(responseData.skillCount).exists;
});
