import { createMockTaskResult } from "../../../test/resource/mock-data"
import { deepEqualSkipOuterType, mismatched } from "../../../test/util/deep-equals"
import { ApiTaskResult, ITaskResult } from "./ApiTaskResult"


describe("ApiTaskResult", () => {
  it("ApiTaskResult should be created", () => {
    // Arrange
    const iTaskResult: ITaskResult = createMockTaskResult()

    // Act
    const apiTaskResult = new ApiTaskResult(iTaskResult)

    // Assert
    expect(apiTaskResult).toBeTruthy()
    expect(deepEqualSkipOuterType(apiTaskResult, iTaskResult)).toBeTruthy(mismatched(apiTaskResult, iTaskResult))
  })
})
