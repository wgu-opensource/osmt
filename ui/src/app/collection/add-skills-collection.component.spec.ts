import { Location } from "@angular/common"
import { HttpClientTestingModule } from "@angular/common/http/testing"
import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { Title } from "@angular/platform-browser"
import { ActivatedRoute, Router } from "@angular/router"
import { RouterTestingModule } from "@angular/router/testing"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import { TestPage } from "test/util/test-page.spec"
import { createMockPaginatedCollections } from "../../../test/resource/mock-data"
import { CollectionServiceStub, EnvironmentServiceStub, RouterStub } from "../../../test/resource/mock-stubs"
import { AppConfig } from "../app.config"
import { initializeApp } from "../app.module"
import { EnvironmentService } from "../core/environment.service"
import { PublishStatus } from "../PublishStatus"
import { ApiSortOrder } from "../richskill/ApiSkill"
import { PaginatedCollections } from "../richskill/service/rich-skill-search.service"
import { ToastService } from "../toast/toast.service"
import { AddSkillsCollectionComponent } from "./add-skills-collection.component"
import { CollectionService } from "./service/collection.service"


export function createComponent(T: Type<AddSkillsCollectionComponent>): Promise<void> {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  fixture.detectChanges()

  return fixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    fixture.detectChanges()
  })
}


let activatedRoute: ActivatedRouteStubSpec
let component: AddSkillsCollectionComponent
let fixture: ComponentFixture<AddSkillsCollectionComponent>


describe("AddSkillsCollectionComponent", () => {
  const search = "testSearchQuery"

  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        AddSkillsCollectionComponent
      ],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        AppConfig,
        Title,
        Location,
        ToastService,
        { provide: EnvironmentService, useClass: EnvironmentServiceStub },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useClass: RouterStub },
        { provide: CollectionService, useClass: CollectionServiceStub },
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()

    activatedRoute.setParamMap({ userId: 126 })
    createComponent(AddSkillsCollectionComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("ngOnInit should be correct", () => {
    expect(component.totalCount).toBeFalsy()
  })

  it("handleDefaultSubmit should load", () => {
    // Arrange
    component.from = 10

    // Act
    const result = component.handleDefaultSubmit()

    // Assert
    expect(result).toBeFalse()
    expect(component.from).toEqual(0)
    // Note that we're not bothering to check the results of loadNextPage()!
  })

  it("loadNextPage should load nothing", () => {
    // Arrange
    const totalCount = createMockPaginatedCollections().totalCount

    // Act
    component.loadNextPage()

    // Assert
    expect(component.results).toBeTruthy()
    if (component.results) {
      expect((component.results as PaginatedCollections).totalCount).toEqual(0)
    }
  })

  it("loadNextPage should load something", () => {
    // Arrange
    component.results = undefined
    component.searchForm.setValue({search})
    const totalCount = createMockPaginatedCollections().totalCount

    // Act
    component.loadNextPage()
    while (!component.results) {}

    // Assert
    expect(component.results).toBeTruthy()
    if (component.results) {
      expect((component.results as PaginatedCollections).totalCount).toEqual(totalCount)
    }
  })

  it("isPlural should be correct", () => {
    // Arrange
    component.state = {
      selectedSkills: [],
      totalCount: 0,
    }
    // Act/Assert
    expect(component.isPlural).toBeFalse()

    // Arrange
    component.state = {
      selectedSkills: [],
      totalCount: 2,
    }
    // Act/Assert
    expect(component.isPlural).toBeTruthy()
  })

  it("clearSearch should clear", () => {
    component.searchForm.setValue({search})
    expect(component.searchQuery).toEqual(search)
    expect(component.clearSearch()).toBeFalse()
    expect(component.searchQuery).toEqual("")
  })

  it("handleFiltersChanged should be correct", () => {
    // Arrange
    component.results = undefined
    component.searchForm.setValue({search})
    const filters = new Set([ PublishStatus.Unarchived, PublishStatus.Deleted ])
    const totalCount = createMockPaginatedCollections().totalCount

    // Act
    component.handleFiltersChanged(filters)
    while (!component.results) {}

    // Assert
    expect(component.results).toBeTruthy()
    if (component.results) {
      expect((component.results as PaginatedCollections).totalCount).toEqual(totalCount)
    }
  })

  it("handlePageClicked should be correct", () => {
    // Arrange
    component.from = 23
    component.size = 17
    const newPage = 42
    const expected = (newPage - 1) * 17
    component.results = undefined
    component.searchForm.setValue({search})
    const totalCount = createMockPaginatedCollections().totalCount

    // Act
    component.handlePageClicked(newPage)
    while (!component.results) {}

    // Assert
    expect(component.from).toEqual(expected)
    expect(component.results).toBeTruthy()
    if (component.results) {
      expect((component.results as PaginatedCollections).totalCount).toEqual(totalCount)
    }
  })

  it("handleHeaderColumnSort should be correct", () => {
    // Arrange
    component.columnSort = ApiSortOrder.SkillAsc
    component.from = 10
    component.results = undefined
    component.searchForm.setValue({search})
    const totalCount = createMockPaginatedCollections().totalCount
    const expected = ApiSortOrder.NameDesc

    // Act
    component.handleHeaderColumnSort(expected)
    while (!component.results) {}

    // Assert
    expect(component.columnSort).toEqual(expected)
    expect(component.from).toEqual(0)
    expect(component.results).toBeTruthy()
    if (component.results) {
      expect((component.results as PaginatedCollections).totalCount).toEqual(totalCount)
    }
  })

  it("firstRecordNo should be correct", () => {
    // Arrange
    component.from = 11
    const expected = 12

    // Act/Assert
    expect(component.firstRecordNo).toEqual(expected)
  })

  it("lastRecordNo should be correct", () => {
    // Arrange
    component.from = 17
    component.results = createMockPaginatedCollections(3, 51)
    const expected = Math.min(17 + 3, 51)

    // Act/Assert
    expect(component.lastRecordNo).toEqual(expected)
  })
})
