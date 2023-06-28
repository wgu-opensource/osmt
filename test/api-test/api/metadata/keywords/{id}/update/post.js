// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();
console.log("Check updated keyword");

pm.test("Check id match", function() {
  pm.expect(responseData.id).to.equal(expectedData.id);
});
pm.test("Check name match", function() {
  pm.expect(responseData.name).to.equal(expectedData.name);
});
pm.test("Check framework match", function() {
  pm.expect(responseData.framework).to.equal(expectedData.framework);
});
pm.test("Check url match", function() {
  pm.expect(responseData.url).to.equal(expectedData.url);
});
pm.test("Check type match", function() {
  pm.expect(responseData.type).to.equal(expectedData.type);
});


