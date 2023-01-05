/* tslint:disable:no-string-literal */
import {HttpClientTestingModule} from "@angular/common/http/testing"
import {Type} from "@angular/core"
import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {Title} from "@angular/platform-browser"
import {ActivatedRoute, Router} from "@angular/router"
import {ActivatedRouteStubSpec} from "test/util/activated-route-stub.spec"
import {createMockPaginatedSkills, createMockSkillSummary, mockTaskResultForExportSearch} from "../../../test/resource/mock-data"
import {AuthServiceStub, RichSkillServiceStub, SearchServiceData, SearchServiceStub} from "../../../test/resource/mock-stubs"
import {AppConfig} from "../app.config"
import {EnvironmentService} from "../core/environment.service"
import {PublishStatus} from "../PublishStatus"
import {ApiAdvancedSearch, ApiSearch} from "../richskill/service/rich-skill-search.service"
import {RichSkillService} from "../richskill/service/rich-skill.service"
import {TableActionDefinition} from "../table/skills-library-table/has-action-definitions"
import {ToastService} from "../toast/toast.service"
import {RichSkillSearchResultsComponent} from "./rich-skill-search-results.component"
import {SearchService} from "./search.service"
import {AuthService} from "../auth/auth-service"
import {of} from "rxjs"
import any = jasmine.any


export function createComponent(T: Type<RichSkillSearchResultsComponent>, f?: () => void): Promise<void> {
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
let component: RichSkillSearchResultsComponent
let fixture: ComponentFixture<RichSkillSearchResultsComponent>


describe("RichSkillSearchResultsComponent", () => {
  let searchService: SearchService

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        RichSkillSearchResultsComponent
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
        { provide: RichSkillService, useClass: RichSkillServiceStub },
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
    createComponent(RichSkillSearchResultsComponent)
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
    advanced.skillName = "test skill"
    advanced.author = "test author"
    const apiSearch = new ApiSearch({ advanced })
    component.matchingQuery = undefined
    const expected = [ "test skill", "test author" ];

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
    expect(component.selectedSkills).toEqual(undefined)
  })
  it("loadNextPage with a filter should return", () => {
    // Arrange
    const size = createMockPaginatedSkills().totalCount
    const query = "my query string"
    component.apiSearch = new ApiSearch({ query })
    component.selectedFilters = new Set<PublishStatus>([PublishStatus.Draft])  // Force search to be called

    // Act
    component.loadNextPage()
    while (!component.results) {}  // Wait for async results

    // Assert
    expect(component.results?.totalCount).toEqual(size)
    expect(component.selectedSkills).toEqual(undefined)
  })

  it("getApiSearch should return", () => {
    // Arrange
    const query = "my query string"
    const skill = createMockSkillSummary()
    const apiSearch = new ApiSearch({ query })
    component.apiSearch = apiSearch
    component.results = createMockPaginatedSkills(component.size, component.size * 2)  // Prep for handleSelectAll call: >1 page
    component.handleSelectAll(false)  // Set component.multiplePagesSelected

    // Act
    const result = component.getApiSearch(skill)

    // Assert
    expect(result).toEqual(apiSearch)
    expect(component.selectedSkills).toEqual(undefined)
  })

  it("getSelectAllCount should return", () => {
    // Arrange
    const skills = createMockPaginatedSkills(component.size, component.size * 2)
    component.results = skills  // Prep for handleSelectAll call: >1 page

    // Act
    const result = component.getSelectAllCount()

    // Assert
    expect(result).toEqual(skills.totalCount)
  })

  it("handleClickAddCollection should return", () => {
    // Arrange
    const router = TestBed.inject(Router)
    const action = new TableActionDefinition({
      label: "Add to Collection",
      callback: undefined
    })
    const skill = createMockSkillSummary()

    // Act
    const result = component.handleClickAddCollection(action, skill)

    // Assert
    expect(result).toBeFalse()
    expect(router.navigate).toHaveBeenCalledWith([ "/collections/add-skills"], any(Object) )
  })

  it("handleClickExportSearch should call subject loader and services methods", () => {
    const spy = spyOn(component["toastService"].loaderSubject, "next")
    const spyExportSearch = spyOn(component["richSkillService"], "exportSearch").and.returnValue(of(mockTaskResultForExportSearch))
    const spyGetResult = spyOn(component["richSkillService"], "getResultExportedLibrary").and.returnValue(of("csv,csv"))
    component["handleClickExportSearch"]()
    expect(spy).toHaveBeenCalled()
    expect(spyExportSearch).toHaveBeenCalled()
    expect(spyGetResult).toHaveBeenCalled()
  })

  it("export search should be visible",
    () => {
      component.selectedSkills = [{
        id: "-http://localhost:8080/api/skills/c97e8705-8b71-4b45-8af5-80ebbbabfb60",
        uuid: "c97e8705-8b71-4b45-8af5-80ebbbabfb60",
        category: ".NET Framework",
        skillName: "Application Domain Creation",
        skillStatement: "Create application domains and assemblies using attributes, formatting and parsing base types, collections, events and exceptions, files and data streams, and generics.",
        status: PublishStatus.Published,
        keywords: [
          ".NET Framework",
          "ADO.NET",
          "WGUSID: 1565",
          "1567"
        ],
        occupations: [],
        publishDate: "2022-02-24T00:27:02"
      }]
      // const spy = spyOn(component["authService"], "isEnabledByRoles").and.returnValue(true)
      expect(component["exportSearchVisible"]()).toBeTrue()
    })
})

describe("RichSkillSearchResultsComponent with latestSearch", () => {
  let searchService: SearchService

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        RichSkillSearchResultsComponent
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
        { provide: RichSkillService, useClass: RichSkillServiceStub },
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
    createComponent(RichSkillSearchResultsComponent, () => {
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

describe("RichSkillSearchResultsComponent with params", () => {
  let searchService: SearchService

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        RichSkillSearchResultsComponent
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
        { provide: RichSkillService, useClass: RichSkillServiceStub },
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
    createComponent(RichSkillSearchResultsComponent, () => {
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

describe("RichSkillSearchResultsComponent with advance search params in history.state", () => {
  let searchService: SearchService
  let advanced: ApiAdvancedSearch

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        RichSkillSearchResultsComponent
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
        { provide: RichSkillService, useClass: RichSkillServiceStub },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useValue: routerSpy },
        { provide: AuthService, useClass: AuthServiceStub },
      ]
    })
      .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName
    searchService = TestBed.inject(SearchService)

    createComponent(RichSkillSearchResultsComponent, () => {
      // Arrange - Setup for ngOnInit alternate path
      advanced = new ApiAdvancedSearch()
      advanced.keywords = ["test keywords"]

      searchService.advancedSkillSearch(advanced)
      history.pushState(SearchServiceData.latestSearch, "advanced")
      component.apiSearch = new ApiSearch({advanced})
    })
  }))

  it("ngOnInit should handle latestSearch", () => {
    // Assert
    expect(component.apiSearch).toBeTruthy()
    expect(history.state.advanced).toBeTruthy()
    expect(history.state.advanced).toEqual({keywords: ["test keywords"]})
  })
})
