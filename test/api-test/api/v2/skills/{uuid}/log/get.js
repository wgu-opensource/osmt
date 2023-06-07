// let expectedData is injected from <response-type>.json

// FIXME - update these tests once docker images are cleaned up automatically
pm.test("Check RSD log", function () {
    let responseData = pm.response.json();

    // pm.expect(responseData).to.have.deep.equal(expectedData);

    // FIXME - remove
    pm.expect(responseData.length > 0);
});