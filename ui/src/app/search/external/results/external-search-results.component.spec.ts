import { HttpClientTestingModule } from "@angular/common/http/testing"
import { async, TestBed } from "@angular/core/testing"
import { of } from "rxjs"
import { Title } from "@angular/platform-browser"
import { AuthService } from "../../../auth/auth-service"
import { ActivatedRoute, Router } from "@angular/router"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import { AppConfig } from "../../../app.config"
import { EnvironmentService } from "../../../core/environment.service"
import { ToastService } from "../../../toast/toast.service"
import { ApiAdvancedSearch, ApiSearch } from "../../../richskill/service/rich-skill-search.service"
import { ExternalSearch, ExternalSearchService } from "../external-search.service"
import { ExternalSearchResultsComponent, SearchResultType } from "./external-search-results.component"
import { AuthServiceStub, ExternalSearchServiceStub } from "../../../../../test/resource/mock-stubs"
import {
  createMockPaginatedCollectionSearchResults,
  createMockPaginatedLibraries
} from "../../../../../test/resource/mock-data"
import { createComponent } from "../../../../../test/util/test-util.spec"

describe("ExternalSearchResultsComponent(Collections)", () => {
  let component: ExternalSearchResultsComponent
  let searchService: ExternalSearchService

  beforeEach(() => {
    // activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async () => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()
    const route = { data: of({ searchResultType: SearchResultType.COLLECTION }) }

    await TestBed.configureTestingModule({
      declarations: [
        ExternalSearchResultsComponent
      ],
      imports: [
        HttpClientTestingModule,  // Needed to avoid the toolName race condition below
      ],
      providers: [
        AppConfig,  // Needed to avoid the toolName race condition below
        EnvironmentService,  // Needed to avoid the toolName race condition below
        Title,
        ToastService,
        { provide: ActivatedRoute, useValue: route },
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: ExternalSearchService, useClass: ExternalSearchServiceStub },
        { provide: Router, useValue: routerSpy },
      ]
    })
    .compileComponents()

    const appConfig: AppConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName
    searchService = TestBed.inject(ExternalSearchService)

    component = await createComponent(ExternalSearchResultsComponent)
  })

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("should handle empty search query", () => {
    // Arrange
    const advancedSearch = new ApiAdvancedSearch()
    const apiSearch = new ApiSearch({advanced: advancedSearch})
    const paginatedLibraries = createMockPaginatedLibraries()
    const externalSearch = new ExternalSearch(apiSearch, paginatedLibraries.libraries)
    component.externalSearch = externalSearch
    searchService.latestSearch = externalSearch

    // Act
    searchService.clearSearch()
    while (component.externalSearch) {}  // wait

    // Assert
    expect(component.externalSearch).toBeFalsy()
  })

  it("should handle search query advanced", () => {
    // Arrange
    const advancedSearch = new ApiAdvancedSearch()
    advancedSearch.skillName = "test skill"
    advancedSearch.author = "test author"
    const paginatedLibraries = createMockPaginatedLibraries()
    const expected = ["test skill", "test author"];

    // Act
    (searchService as unknown as ExternalSearchServiceStub)
      .setLatestSearch(advancedSearch, paginatedLibraries.libraries)
    while (!component.matchingQuery) {}  // wait

    // Assert
    expect(component.matchingQuery).toEqual(expected)
  })

  it("should handle latestSearch", async () => {
    // Arrange
    const advancedSearch = new ApiAdvancedSearch()
    const apiSearch = new ApiSearch({advanced: advancedSearch})
    const paginatedLibraries = createMockPaginatedLibraries()
    const externalSearch = new ExternalSearch(apiSearch, paginatedLibraries.libraries)

    const updateLatestSearch = () => {
      component.externalSearch = undefined
      searchService.latestSearch = externalSearch
    }

    // Act
    component = await createComponent(ExternalSearchResultsComponent, updateLatestSearch)

    // Assert
    expect(component.externalSearch).toBeTruthy()
    expect(component.externalSearch).toEqual(externalSearch)
  })

  it("loadNextPage with no filter should return", () => {
    // Arrange

    // Act
    component.loadNextPage()

    // Assert
    expect(component.totalCount).toEqual(0)
  })

  it("loadNextPage with a filter should return", () => {
    // Arrange
    const totalCount = createMockPaginatedCollectionSearchResults().totalCount
    const advancedSearch = new ApiAdvancedSearch()
    const apiSearch = new ApiSearch({advanced: advancedSearch})
    const paginatedLibraries = createMockPaginatedLibraries()
    component.externalSearch = new ExternalSearch(apiSearch, paginatedLibraries.libraries)

    // Act
    component.loadNextPage()
    while (!component.results) {}  // Wait for async results

    // Assert
    expect(component.totalCount).toEqual(totalCount)
  })
})

describe("ExternalSearchResultsComponent(RSDs)", () => {
  let component: ExternalSearchResultsComponent
  let searchService: ExternalSearchService

  beforeEach(() => {
    // activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async () => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()
    const route = { data: of({ searchResultType: SearchResultType.RSD }) }

    await TestBed.configureTestingModule({
      declarations: [
        ExternalSearchResultsComponent
      ],
      imports: [
        HttpClientTestingModule,  // Needed to avoid the toolName race condition below
      ],
      providers: [
        AppConfig,  // Needed to avoid the toolName race condition below
        EnvironmentService,  // Needed to avoid the toolName race condition below
        Title,
        ToastService,
        { provide: ActivatedRoute, useValue: route },
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: ExternalSearchService, useClass: ExternalSearchServiceStub },
        { provide: Router, useValue: routerSpy },
      ]
    })
    .compileComponents()

    const appConfig: AppConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName
    searchService = TestBed.inject(ExternalSearchService)

    component = await createComponent(ExternalSearchResultsComponent)
  })

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("should handle empty search query", () => {
    // Arrange
    const advancedSearch = new ApiAdvancedSearch()
    const apiSearch = new ApiSearch({advanced: advancedSearch})
    const paginatedLibraries = createMockPaginatedLibraries()
    const externalSearch = new ExternalSearch(apiSearch, paginatedLibraries.libraries)
    component.externalSearch = externalSearch
    searchService.latestSearch = externalSearch

    // Act
    searchService.clearSearch()
    while (component.externalSearch) {}  // wait

    // Assert
    expect(component.externalSearch).toBeFalsy()
  })

  it("should handle search query advanced", () => {
    // Arrange
    const advancedSearch = new ApiAdvancedSearch()
    advancedSearch.skillName = "test skill"
    advancedSearch.author = "test author"
    const paginatedLibraries = createMockPaginatedLibraries()
    const expected = ["test skill", "test author"];

    // Act
    (searchService as unknown as ExternalSearchServiceStub)
      .setLatestSearch(advancedSearch, paginatedLibraries.libraries)
    while (!component.matchingQuery) {}  // wait

    // Assert
    expect(component.matchingQuery).toEqual(expected)
  })

  it("should handle latestSearch", async () => {
    // Arrange
    const advancedSearch = new ApiAdvancedSearch()
    const apiSearch = new ApiSearch({advanced: advancedSearch})
    const paginatedLibraries = createMockPaginatedLibraries()
    const externalSearch = new ExternalSearch(apiSearch, paginatedLibraries.libraries)

    // Act
    component = await createComponent(
      ExternalSearchResultsComponent,
      (c) => {
        c.externalSearch = undefined
        searchService.latestSearch = externalSearch
      }
    )

    // Assert
    expect(component.externalSearch).toBeTruthy()
    expect(component.externalSearch).toEqual(externalSearch)
  })

  it("loadNextPage with no filter should return", () => {
    // Arrange

    // Act
    component.loadNextPage()

    // Assert
    expect(component.totalCount).toEqual(0)
  })

  it("loadNextPage with a filter should return", () => {
    // Arrange
    const totalCount = createMockPaginatedCollectionSearchResults().totalCount
    const advancedSearch = new ApiAdvancedSearch()
    const apiSearch = new ApiSearch({advanced: advancedSearch})
    const paginatedLibraries = createMockPaginatedLibraries()
    component.externalSearch = new ExternalSearch(apiSearch, paginatedLibraries.libraries)

    // Act
    component.loadNextPage()
    while (!component.results) {}  // Wait for async results

    // Assert
    expect(component.totalCount).toEqual(totalCount)
  })
})
