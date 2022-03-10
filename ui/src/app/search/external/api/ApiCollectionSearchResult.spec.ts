import {
  ApiCollectionSearchResult,
  ICollectionSearchResult,
  PaginatedCollectionSearchResults
} from "./ApiCollectionSearchResult"
import {deepEqualSkipOuterType, mismatched} from "../../../../../test/util/deep-equals"
import {
  createMockApiCollectionSearchResult,
  createMockPaginatedCollectionSearchResults
} from "../../../../../test/resource/mock-data"


describe("ApiCollectionSearchResult", () => {
  it("ApiCollectionSearchResult should be created", () => {
    // Arrange
    const iCollectionSearchResult: ICollectionSearchResult = createMockApiCollectionSearchResult()

    // Act
    const apiCollectionSearchResult = new ApiCollectionSearchResult(iCollectionSearchResult)

    // Assert
    expect(apiCollectionSearchResult).toBeTruthy()
    expect(deepEqualSkipOuterType(apiCollectionSearchResult, iCollectionSearchResult))
      .toBeTruthy(mismatched(apiCollectionSearchResult, iCollectionSearchResult))
  })

  it("PaginatedCollectionSearchResults should be created", () => {
    // Arrange
    const collectionCount = 3
    const totalCollectionCount = 7
    const origCollectionSearchResults: PaginatedCollectionSearchResults = createMockPaginatedCollectionSearchResults(
      collectionCount,
      totalCollectionCount
    )

    // Act
    const collectionSearchResults = new PaginatedCollectionSearchResults(
      origCollectionSearchResults.results,
      origCollectionSearchResults.totalCount
    )

    // Assert
    expect(collectionSearchResults).toBeTruthy()
    expect(collectionSearchResults.results?.length).toEqual(collectionCount)
    expect(collectionSearchResults.totalCount).toEqual(totalCollectionCount)
    expect(deepEqualSkipOuterType(collectionSearchResults, origCollectionSearchResults))
      .toBeTruthy(mismatched(collectionSearchResults, origCollectionSearchResults))
  })
})
