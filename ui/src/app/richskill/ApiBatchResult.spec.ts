import { createMockBatchResult } from "test/resource/mock-data"
import { deepEqualSkipOuterType, mismatched } from "../../../test/util/deep-equals"
import { ApiBatchResult, IBatchResult } from "./ApiBatchResult"


describe("BatchResult", () => {
  it("BatchResult should be created", () => {
    // Arrange
    const iBatchResult: IBatchResult = createMockBatchResult()

    // Act
    const apiBatchResult = new ApiBatchResult(iBatchResult)

    // Assert
    expect(apiBatchResult).toBeTruthy()
    expect(deepEqualSkipOuterType(apiBatchResult, iBatchResult)).toBeTruthy(mismatched(apiBatchResult, iBatchResult))
  })
})

