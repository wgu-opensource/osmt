// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();
console.log("Check categories");
pm.test("Check categories count", function() {
    pm.expect(responseData.length > 0);
});

for(let categoriesIndex = 0; categoriesIndex < responseData.length; categoriesIndex++) {
    let catNum = categoriesIndex + 1;
    let responseCat = responseData[0];

    pm.test(`Category ${catNum} - Check value exists`, function () {
        pm.expect(responseCat.value).exists;
    });
    pm.test(`Category ${catNum} - Check id exists`, function () {
        pm.expect(responseCat.id).exists;
    });
    pm.test(`Category ${catNum} - Check type exists`, function () {
        pm.expect(responseCat.type).exists;
    });
    pm.test(`Category ${catNum} - Check skillCount exists`, function () {
        pm.expect(responseCat.skillCount).exists;
    });
}
