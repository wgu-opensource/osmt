// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();
console.log("Check xlsx generation task submission");
pm.test("Check task UUID created", function () {
  pm.expect(responseData.uuid).exists;
});
pm.test("Check task status", function () {
  pm.expect(responseData.status).to.equal(expectedData.status);
});
pm.test("Check task output content type", function () {
  pm.expect(responseData['content-type']).to.equal(expectedData['content-type']);
});
pm.test("Check result endpoint provided", function () {
  pm.expect(responseData.id).exists;
});