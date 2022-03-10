import {ApiSkillSearchResult, ISkillSearchResult, PaginatedSkillSearchResults} from "./ApiSkillSearchResult"
import {deepEqualSkipOuterType, mismatched} from "../../../../../test/util/deep-equals"
import {
  createMockApiSkillSearchResult,
  createMockPaginatedSkillSearchResults, createMockSkillSummary
} from "../../../../../test/resource/mock-data"
import {ISkillSummary} from "../../../richskill/ApiSkillSummary";


describe("ApiSkillSearchResult", () => {
  it("ApiSkillSearchResult should be created", () => {
    // Arrange
    const skill: ISkillSummary = createMockSkillSummary()
    const iSkillSearchResult: ISkillSearchResult = createMockApiSkillSearchResult(skill, true)

    // Act
    const apiSkillSearchResult = new ApiSkillSearchResult(iSkillSearchResult)

    // Assert
    expect(apiSkillSearchResult).toBeTruthy()
    expect(apiSkillSearchResult.similarToLocalSkill).toBeTruthy()
    expect(deepEqualSkipOuterType(apiSkillSearchResult, iSkillSearchResult))
      .toBeTruthy(mismatched(apiSkillSearchResult, iSkillSearchResult))
  })

  it("PaginatedSkillSearchResults should be created", () => {
    // Arrange
    const skillCount = 3
    const totalSkillCount = 7
    const origSkillSearchResults: PaginatedSkillSearchResults = createMockPaginatedSkillSearchResults(
      skillCount,
      totalSkillCount
    )

    // Act
    const skillSearchResults = new PaginatedSkillSearchResults(
      origSkillSearchResults.results,
      origSkillSearchResults.totalCount
    )

    // Assert
    expect(skillSearchResults).toBeTruthy()
    expect(skillSearchResults.results?.length).toEqual(skillCount)
    expect(skillSearchResults.totalCount).toEqual(totalSkillCount)
    expect(deepEqualSkipOuterType(skillSearchResults, origSkillSearchResults))
      .toBeTruthy(mismatched(skillSearchResults, origSkillSearchResults))
  })
})
