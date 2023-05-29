// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();
console.log("Check category skill count");
pm.test("Check category skill count", function() {
    pm.expect(responseData.length > 0);
});
