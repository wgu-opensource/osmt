import { createMockCollectionSummary, createMockSkillSummary } from "../../../test/resource/mock-data"
import { deepEqualSkipOuterType, mismatched } from "../../../test/util/deep-equals"
import { ApiCollectionSummary, ApiSkillSummary, ISkillSummary } from "./ApiSkillSummary"


describe("ApiSkillSummary", () => {
  it("ApiSkillSummary should be created", () => {
    // Arrange
    const iSkillSummary: ISkillSummary = createMockSkillSummary()

    // Act
    const apiSkillSummary = new ApiSkillSummary(iSkillSummary)

    // Assert
    expect(apiSkillSummary).toBeTruthy()
    expect(deepEqualSkipOuterType(apiSkillSummary, iSkillSummary)).toBeTruthy(mismatched(apiSkillSummary, iSkillSummary))
  })


  it("ApiCollectionSummary should be created", () => {
    // Arrange
    const iCollectionSummary: ApiCollectionSummary = createMockCollectionSummary()

    // Act
    const apiCollectionSummary = new ApiCollectionSummary(iCollectionSummary)

    // Assert
    expect(apiCollectionSummary).toBeTruthy()
    expect(deepEqualSkipOuterType(apiCollectionSummary, iCollectionSummary))
      .toBeTruthy(mismatched(apiCollectionSummary, iCollectionSummary))
  })
})
