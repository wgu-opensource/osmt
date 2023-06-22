// let expectedData is injected from <response-type>.json

let responseData = pm.response.json();

pm.test("Check RSD log count", function () {
  pm.expect(responseData.length > 0);
});

for (let logIndex = 0; logIndex < expectedData.length; logIndex++) {
  let expectedLog = expectedData[logIndex];
  let logNum = logIndex + 1;

  let responseLog = responseData[logIndex];
  pm.test(`Log ${logNum} - Check User exists`, function () {
    pm.expect(responseLog.user).exists;
  });
  pm.test(`Log ${logNum} - Check Changed Fields exists`, function () {
    pm.expect(responseLog.changedFields).exists;
  });
  pm.test(`Log ${logNum} - Check Operation Type exists`, function () {
    pm.expect(responseLog.operationType).exists;
  });
  pm.test(`Log ${logNum} - Check Creation Date exists`, function () {
    pm.expect(responseLog.creationDate).exists;
  });
}