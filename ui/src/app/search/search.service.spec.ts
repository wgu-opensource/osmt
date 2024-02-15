import {async, TestBed} from "@angular/core/testing"
import { ApiAdvancedSearch, ApiSearch } from "../richskill/service/rich-skill-search.service"
import {SearchService} from "./search.service"
import {Router} from "@angular/router"
import { RouterData, RouterStub } from "../../../test/resource/mock-stubs"


describe("SearchService", () => {
  let service: SearchService
  beforeEach(async(() => {

    TestBed.configureTestingModule({
      providers: [
        SearchService,
        { provide: Router, useClass: RouterStub},
      ]
    })
    service = TestBed.inject(SearchService)
    // .compileComponents()

  }))

  it("should be created", () => {
    expect(service).toBeTruthy()
  })

  it("should perform simple skill search", () => {
    // Arrange
    let result: ApiSearch | undefined
    const query = "testQuery"
    RouterData.commands = []
    RouterData.extras = {}

    service.searchQuery$.subscribe((msg) => {
      result = msg
      const expected = new ApiSearch({query})
      expect(msg).toEqual(expected)
    })

    // Act
    service.simpleSkillSearch(query)

    // Assert
    expect(RouterData.commands).toEqual(["/skills/search"])
    expect(RouterData.extras).toEqual({queryParams: {q: query}})
  })

  it("should perform advanced skill search", () => {
    // Arrange
    let result: ApiSearch | undefined
    // const query = "testQuery"
    const advanced = new ApiAdvancedSearch()
    RouterData.commands = []
    RouterData.extras = {}

    service.searchQuery$.subscribe((msg) => {
      result = msg
      const expected = new ApiSearch({advanced})
      expect(msg).toEqual(expected)
    })

    // Act
    service.advancedSkillSearch(advanced)

    // Assert
    expect(RouterData.commands).toEqual(["/skills/search"])
  })

  it( "should pass advanced query to history.state via router navigation", () => {
    // Arrange
    const advanced = new ApiAdvancedSearch()
    advanced.keywords = ["test keywords"]
    const apiSearch = new ApiSearch({advanced});
    RouterData.commands = []
    RouterData.extras = {}

    // Act
    service.advancedSkillSearch(advanced)

    // Assert
    expect(RouterData.commands).toEqual(["/skills/search"])
    expect(RouterData.extras).toEqual({state: apiSearch})
  })

  it("should perform simple collection search", () => {
    // Arrange
    let result: ApiSearch | undefined
    const query = "testQuery"
    RouterData.commands = []
    RouterData.extras = {}

    service.searchQuery$.subscribe((msg) => {
      result = msg
      const expected = new ApiSearch({query})
      expect(msg).toEqual(expected)
    })

    // Act
    service.simpleCollectionSearch(query)

    // Assert
    expect(RouterData.commands).toEqual(["/collections/search"])
    expect(RouterData.extras).toEqual({queryParams: {q: query}})
    // expect(service.simpleCollectionSearch("foo")).toHaveBeenCalledWith(["foo"], {queryParams: {extras: "foo"}})
  })

  it("should perform advanced collection search", () => {
    // Arrange
    let result: ApiSearch | undefined
    // const query = "testQuery"
    const advanced = new ApiAdvancedSearch()
    RouterData.commands = []
    RouterData.extras = {}

    service.searchQuery$.subscribe((msg) => {
      result = msg
      const expected = new ApiSearch({advanced})
      expect(msg).toEqual(expected)
    })

    // Act
    service.advancedCollectionSearch(advanced)

    // Assert
    expect(RouterData.commands).toEqual(["/collections/search"])
  })

  it("should set/clear search", () => {
    let result: ApiSearch | undefined
    const query = "testQuery"
    const expected = new ApiSearch({query})

    service.searchQuery$.subscribe((msg) => {
      result = msg
    })

    // Set searchQuery$
    service.simpleSkillSearch(query)
    while (!result) { }  // wait
    expect(result).toEqual(expected)

    // Clear searchQuery$
    service.clearSearch()
    while (result) { } // wait
    expect(service.latestSearch).toBeFalsy()
  })
})
