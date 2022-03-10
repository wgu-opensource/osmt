import {ApiLibrarySummary, ILibrarySummary, PaginatedLibraries} from "./ApiLibrary"
import {deepEqualSkipOuterType, mismatched} from "../../../../../test/util/deep-equals"
import {createMockLibrarySummary, createMockPaginatedLibraries} from "../../../../../test/resource/mock-data"


describe("ApiLibrary", () => {
  it("ApiLibrarySummary should be created", () => {
    // Arrange
    const iLibrarySummary: ILibrarySummary = createMockLibrarySummary()

    // Act
    const apiLibrarySummary = new ApiLibrarySummary(iLibrarySummary)

    // Assert
    expect(apiLibrarySummary).toBeTruthy()
    expect(deepEqualSkipOuterType(apiLibrarySummary, iLibrarySummary))
      .toBeTruthy(mismatched(apiLibrarySummary, iLibrarySummary))
  })

  it("PaginatedLibraries should be created", () => {
    // Arrange
    const libraryCount = 3
    const totalLibraryCount = 7
    const origLibraries: PaginatedLibraries = createMockPaginatedLibraries(libraryCount, totalLibraryCount)

    // Act
    const libraries = new PaginatedLibraries(origLibraries.libraries, origLibraries.totalCount)

    // Assert
    expect(libraries).toBeTruthy()
    expect(libraries.libraries?.length).toEqual(libraryCount)
    expect(libraries.totalCount).toEqual(totalLibraryCount)
    expect(deepEqualSkipOuterType(libraries, origLibraries))
      .toBeTruthy(mismatched(libraries, origLibraries))
  })
})
