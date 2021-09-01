import { createMockJobcode } from "test/resource/mock-data"
import { deepEqualSkipOuterType, mismatched } from "../../../test/util/deep-equals"
import { ApiJobCode, IJobCode } from "./Jobcode"


describe("Jobcode", () => {
  it("ApiJobcode should be created", () => {
    // Arrange
    const iJobcode: IJobCode = createMockJobcode()

    // Act
    const apiJobcode = new ApiJobCode(iJobcode)

    // Assert
    expect(apiJobcode).toBeTruthy()
    expect(deepEqualSkipOuterType(apiJobcode, iJobcode)).toBeTruthy(mismatched(apiJobcode, iJobcode))
  })
})

