import { HttpClientTestingModule } from "@angular/common/http/testing"
import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { Title } from "@angular/platform-browser"
import { ActivatedRoute, Router } from "@angular/router"
import { AppConfig } from "src/app/app.config"
import { EnvironmentService } from "src/app/core/environment.service"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import { createMockCollectionSummary, createMockPaginatedCollections } from "../../../test/resource/mock-data"
import {AuthServiceStub, CollectionServiceStub, SearchServiceStub} from "../../../test/resource/mock-stubs"
import { PublishStatus } from "../PublishStatus"
import { ApiAdvancedSearch, ApiSearch } from "../richskill/service/rich-skill-search.service"
import { SearchService } from "../search/search.service"
import { ToastService } from "../toast/toast.service"
import { CollectionSearchResultsComponent } from "./collection-search-results.component"
import { CollectionService } from "./service/collection.service"
import {AuthService} from "../auth/auth-service";


export function createComponent(T: Type<CollectionSearchResultsComponent>, f?: () => void): Promise<void> {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance

  if (f) {
    f()
  }

  // 1st change detection triggers ngOnInit which gets a hero
  fixture.detectChanges()

  return fixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    fixture.detectChanges()
  })
}


let activatedRoute: ActivatedRouteStubSpec
let component: CollectionSearchResultsComponent
let fixture: ComponentFixture<CollectionSearchResultsComponent>


describe("CollectionSearchResultsComponent", () => {
  let searchService: SearchService

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        CollectionSearchResultsComponent
      ],
      imports: [
        HttpClientTestingModule,  // Needed to avoid the toolName race condition below
      ],
      providers: [
        EnvironmentService,  // Needed to avoid the toolName race condition below
        AppConfig,  // Needed to avoid the toolName race condition below
        Title,
        ToastService,
        { provide: SearchService, useClass: SearchServiceStub },
        { provide: CollectionService, useClass: CollectionServiceStub },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useValue: routerSpy },
        { provide: AuthService, useClass: AuthServiceStub },
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName
    searchService = TestBed.inject(SearchService)

    activatedRoute.setParams({ query: "some query" })
    createComponent(CollectionSearchResultsComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("should handle empty search query", () => {
    // Arrange
    const query = "some query"
    const apiSearch = new ApiSearch({ query })
    component.apiSearch = apiSearch
    searchService.latestSearch = apiSearch

    // Act
    searchService.clearSearch()
    while (component.apiSearch) {}  // wait

    // Assert
    expect(component.apiSearch).toBeFalsy()
  })
  it("should handle search query", () => {
    // Arrange
    const query = "some query"
    const apiSearch = new ApiSearch({ query })
    component.matchingQuery = undefined;

    // Act
    (searchService as unknown as SearchServiceStub).setLatestSearch(apiSearch)
    while (!component.matchingQuery) {}  // wait

    // Assert
    expect(component.matchingQuery).toEqual([query])
  })
  it("should handle search query advanced", () => {
    // Arrange
    const advanced = new ApiAdvancedSearch()
    advanced.collectionName = "test collection"
    advanced.author = "test author"
    const apiSearch = new ApiSearch({ advanced })
    component.matchingQuery = undefined
    const expected = [ "test collection", "test author" ];

    // Act
    (searchService as unknown as SearchServiceStub).setLatestSearch(apiSearch)
    while (!component.matchingQuery) {}  // wait

    // Assert
    expect(component.matchingQuery).toEqual(expected)
  })

  it("loadNextPage with no filter should return", () => {
    // Arrange
    component.selectedFilters = new Set<PublishStatus>()  // Force setResults to be called

    // Act
    component.loadNextPage()

    // Assert
    expect(component.results?.totalCount).toEqual(0)
    expect(component.selectedCollections).toEqual(undefined)
  })
  it("loadNextPage with a filter should return", () => {
    // Arrange
    const size = createMockPaginatedCollections().totalCount
    const query = "my query string"
    component.apiSearch = new ApiSearch({ query })
    component.selectedFilters = new Set<PublishStatus>([PublishStatus.Draft])  // Force search to be called

    // Act
    component.loadNextPage()
    while (!component.results) {}  // Wait for async results

    // Assert
    expect(component.results?.totalCount).toEqual(size)
    expect(component.selectedCollections).toEqual(undefined)
  })

  it("getApiSearch should return", () => {
    // Arrange
    const query = "my query string"
    const collection = createMockCollectionSummary()
    const apiSearch = new ApiSearch({ query })
    component.apiSearch = apiSearch
    component.results = createMockPaginatedCollections(component.size, component.size * 2)  // Prep for handleSelectAll call: >1 page
    component.handleSelectAll(false)  // Set component.multiplePagesSelected

    // Act
    const result = component.getApiSearch(collection)

    // Assert
    expect(result).toEqual(apiSearch)
    expect(component.selectedCollections).toEqual(undefined)
  })

  it("getSelectAllCount should return", () => {
    // Arrange
    const collections = createMockPaginatedCollections(component.size, component.size * 2)
    component.results = collections  // Prep for handleSelectAll call: >1 page

    // Act
    const result = component.getSelectAllCount()

    // Assert
    expect(result).toEqual(collections.totalCount)
  })
})

describe("RichCollectionSearchResultsComponent with latestSearch", () => {
  let searchService: SearchService

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        CollectionSearchResultsComponent
      ],
      imports: [
        HttpClientTestingModule,  // Needed to avoid the toolName race condition below
      ],
      providers: [
        EnvironmentService,  // Needed to avoid the toolName race condition below
        AppConfig,  // Needed to avoid the toolName race condition below
        Title,
        ToastService,
        { provide: SearchService, useClass: SearchServiceStub },
        { provide: CollectionService, useClass: CollectionServiceStub },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useValue: routerSpy },
        { provide: AuthService, useClass: AuthServiceStub },
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName
    searchService = TestBed.inject(SearchService)

    activatedRoute.setParams({ query: "some query" })
    createComponent(CollectionSearchResultsComponent, () => {
      // Arrange - Setup for ngOnInit alternate path
      const query = "some query"
      searchService.latestSearch = new ApiSearch({ query })
      component.apiSearch = undefined
    })
  }))

  it("ngOnInit should handle latestSearch", () => {
    // Assert
    expect(component.apiSearch).toBeTruthy()
  })
})

describe("RichCollectionSearchResultsComponent with params", () => {
  let searchService: SearchService

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        CollectionSearchResultsComponent
      ],
      imports: [
        HttpClientTestingModule,  // Needed to avoid the toolName race condition below
      ],
      providers: [
        EnvironmentService,  // Needed to avoid the toolName race condition below
        AppConfig,  // Needed to avoid the toolName race condition below
        Title,
        ToastService,
        { provide: SearchService, useClass: SearchServiceStub },
        { provide: CollectionService, useClass: CollectionServiceStub },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useValue: routerSpy },
        { provide: AuthService, useClass: AuthServiceStub },
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName
    searchService = TestBed.inject(SearchService)

    activatedRoute.setQueryParams({ q: "some query" })
    createComponent(CollectionSearchResultsComponent, () => {
      // Arrange - Setup for ngOnInit alternate path
      const advanced = new ApiAdvancedSearch()
      component.apiSearch = new ApiSearch({ advanced })
    })
  }))

  it("ngOnInit should handle latestSearch", () => {
    // Assert
    expect(component.apiSearch).toBeTruthy()
  })
})
