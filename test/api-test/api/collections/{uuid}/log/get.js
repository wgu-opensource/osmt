// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

console.log("Check logs");

// FIXME - update these tests once docker images are cleaned up automatically
pm.test("Check logs count", function () {
  pm.expect(responseData.length > 0);
});

for (let logIndex = 0; logIndex < expectedData.length; logIndex++) {
  let expectedLog = expectedData[0];
  let logNum = logIndex + 1;

  let responseLog = responseData[0];
  pm.test(`Log ${logNum} - Check log creation date exists`, function () {
    pm.expect(responseLog.creationDate).exists;
  });
  pm.test(`Log ${logNum} - Check operation type`, function () {
    pm.expect(responseLog.operationType).exists;
  });
  pm.test(`Log ${logNum} - Check user exists`, function () {
    pm.expect(responseLog.user).exists;
  });
  pm.test(`Log ${logNum} - Check changed fields exists`, function () {
    pm.expect(responseLog.changedFields).exists;
  });
}