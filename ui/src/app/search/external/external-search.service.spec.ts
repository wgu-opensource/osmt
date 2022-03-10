import { async, TestBed } from "@angular/core/testing"
import { ApiAdvancedSearch, ApiSearch } from "../../richskill/service/rich-skill-search.service"
import { ExternalSearch, ExternalSearchService } from "./external-search.service"
import { Router } from "@angular/router"
import {
  AuthServiceData,
  AuthServiceStub,
  RouterData,
  RouterStub,
} from "../../../../test/resource/mock-stubs"
import { HttpClient } from "@angular/common/http"
import { HttpClientTestingModule, HttpTestingController } from "@angular/common/http/testing"
import { EnvironmentService } from "../../core/environment.service"
import { AppConfig } from "../../app.config"
import { Location } from "@angular/common"
import { AuthService } from "../../auth/auth-service"
import {
  createMockPaginatedCollectionSearchResults,
  createMockPaginatedLibraries, createMockPaginatedSkillSearchResults
} from "../../../../test/resource/mock-data"

import { PaginatedLibraries } from "./api/ApiLibrary"
import { PaginatedCollectionSearchResults } from "./api/ApiCollectionSearchResult"
import { PaginatedSkillSearchResults } from "./api/ApiSkillSearchResult"


describe("ExternalSearchService", () => {
  let appConfig: AppConfig
  let httpClient: HttpClient
  let httpTestingController: HttpTestingController
  let service: ExternalSearchService

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [],
      imports: [
        HttpClientTestingModule
      ],
      providers: [
        EnvironmentService,
        AppConfig,
        ExternalSearchService,
        Location,
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: Router, useClass: RouterStub }
      ]
    })

    appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()

    httpClient = TestBed.inject(HttpClient)
    httpTestingController = TestBed.inject(HttpTestingController)
    service = TestBed.inject(ExternalSearchService)
    return
  }))

  it("should be created", () => {
    expect(service).toBeTruthy()
  })

  it("should get external libraries", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const path = "api/external/libraries"
    const testData: PaginatedLibraries = createMockPaginatedLibraries(5, 5)

    // Act
    const result$ = service.getLibraries()

    // Assert
    result$
      .subscribe((data: PaginatedLibraries) => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("GET")
    req.flush(testData.libraries, {
      headers: { "x-total-count": "" + testData.totalCount}
    })
  })

  it("should search collections", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const paginatedLibraries = createMockPaginatedLibraries()
    const advancedSearch = new ExternalSearch(new ApiSearch({}), paginatedLibraries.libraries)
    const size = 5
    const from = 10
    const totalCount = 20
    const path = `api/external/search/collections?size=${size}&from=${from}`
    const testData: PaginatedCollectionSearchResults = createMockPaginatedCollectionSearchResults(size, totalCount)

    // Act
    const result$ = service.searchCollections(advancedSearch, size, from)

    // Assert
    result$
      .subscribe((data: PaginatedCollectionSearchResults) => {
        expect(data.results?.length).toEqual(size)
        expect(data.totalCount).toEqual(totalCount)
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("POST")
    req.flush(testData.results, {
      headers: { "x-total-count": "" + testData.totalCount}
    })
  })

  it("should search skills", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const paginatedLibraries = createMockPaginatedLibraries()
    const externalSearch = new ExternalSearch(new ApiSearch({}), paginatedLibraries.libraries)
    const size = 5
    const from = 10
    const totalCount = 20
    const path = `api/external/search/skills?size=${size}&from=${from}`
    const testData: PaginatedSkillSearchResults = createMockPaginatedSkillSearchResults(size, totalCount)

    // Act
    const result$ = service.searchSkills(externalSearch, size, from)

    // Assert
    result$
      .subscribe((data: PaginatedSkillSearchResults) => {
        expect(data.results?.length).toEqual(size)
        expect(data.totalCount).toEqual(totalCount)
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("POST")
    req.flush(testData.results, {
      headers: { "x-total-count": "" + testData.totalCount}
    })
  })

  it("should navigate to advanced collection search results", () => {
    // Arrange
    let result: ExternalSearch | undefined
    const advanced = new ApiAdvancedSearch()
    const paginatedLibraries = createMockPaginatedLibraries()
    RouterData.commands = []
    RouterData.extras = {}

    service.searchQuery$.subscribe((msg) => {
      result = msg
      const expected = new ExternalSearch(new ApiSearch({advanced}), paginatedLibraries.libraries)
      expect(msg).toEqual(expected)
    })

    // Act
    service.advancedCollectionSearch(advanced, paginatedLibraries.libraries)

    // Assert
    expect(RouterData.commands).toEqual(["/collections/search/external"])
  })

  it("should navigate to advanced skill search results", () => {
    // Arrange
    let result: ExternalSearch | undefined
    const advanced = new ApiAdvancedSearch()
    const paginatedLibraries = createMockPaginatedLibraries()
    RouterData.commands = []
    RouterData.extras = {}

    service.searchQuery$.subscribe((msg) => {
      result = msg
      const expected = new ExternalSearch(new ApiSearch({advanced}), paginatedLibraries.libraries)
      expect(msg).toEqual(expected)
    })

    // Act
    service.advancedSkillSearch(advanced, paginatedLibraries.libraries)

    // Assert
    expect(RouterData.commands).toEqual(["/skills/search/external"])
  })

  it("should set/clear search", () => {
    let result: ExternalSearch | undefined
    const paginatedLibraries = createMockPaginatedLibraries()
    const advancedSearch = new ApiAdvancedSearch()
    const apiSearch = new ApiSearch({ advanced: advancedSearch })
    const expected = new ExternalSearch(apiSearch, paginatedLibraries.libraries)

    service.searchQuery$.subscribe((msg) => {
      result = msg
    })

    // Set searchQuery$
    service.advancedCollectionSearch(advancedSearch, paginatedLibraries.libraries)
    while (!result) { }  // wait
    expect(result).toEqual(expected)

    // Clear searchQuery$
    service.clearSearch()
    while (result) { } // wait
    expect(service.latestSearch).toBeFalsy()
  })
})
