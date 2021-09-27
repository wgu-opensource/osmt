import { HttpClientTestingModule } from "@angular/common/http/testing"
import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { Title } from "@angular/platform-browser"
import { ActivatedRoute, Router } from "@angular/router"
import { RouterTestingModule } from "@angular/router/testing"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import { createMockPaginatedSkills } from "../../../../../test/resource/mock-data"
import { CollectionServiceStub, RichSkillServiceStub } from "../../../../../test/resource/mock-stubs"
import { AppConfig } from "../../../app.config"
import { EnvironmentService } from "../../../core/environment.service"
import { ApiSortOrder } from "../../../richskill/ApiSkill"
import { RichSkillService } from "../../../richskill/service/rich-skill.service"
import { ToastService } from "../../../toast/toast.service"
import { CollectionService } from "../../service/collection.service"
import { CollectionPublicComponent } from "./collection-public.component"


export function createComponent(T: Type<CollectionPublicComponent>): Promise<void> {
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
let component: CollectionPublicComponent
let fixture: ComponentFixture<CollectionPublicComponent>


describe("CollectionPublicComponent", () => {
  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        CollectionPublicComponent
      ],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        AppConfig,
        EnvironmentService,
        Title,
        ToastService,
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useValue: routerSpy },
        { provide: RichSkillService, useClass: RichSkillServiceStub },
        { provide: CollectionService, useClass: CollectionServiceStub },
      ]
    }).compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()

    activatedRoute.setParams({ uuid: "uuid1" })
    createComponent(CollectionPublicComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
    expect(component.collection).toBeTruthy()
    expect(component.resultsLoaded).toBeTruthy()
  })

  it("title should be correct", () => {
    const titleService = TestBed.inject(Title)
    expect(titleService.getTitle()).toEqual("my collection name | Collection | OSMT")
  })

  it("totalCount should be correct", () => {
    const results = createMockPaginatedSkills()  // Loaded by ngOnInit
    expect(component.totalCount).toEqual(results.totalCount)
  })

  it("emptyResults should be correct", () => {
    expect(component.emptyResults).toBeFalsy()
  })

  it("curPageCount should be correct", () => {
    const results = createMockPaginatedSkills()  // Loaded by ngOnInit
    expect(component.curPageCount).toEqual(results.skills.length)
  })

  it("totalPageCount should be correct", () => {
    const totalPageCount = 1  // Loaded by ngOnInit
    expect(component.totalPageCount).toEqual(totalPageCount)
  })

  it("currentPageNo should be correct", () => {
    const curPageCount = 1  // Loaded by ngOnInit
    expect(component.currentPageNo).toEqual(curPageCount)
  })

  it("collectionUrl should be correct", () => {
    expect(component.collectionUrl).toEqual("id1")
  })

  it("collectionUuid should be correct", () => {
    const curPageCount = 1  // Loaded by ngOnInit
    expect(component.collectionUuid).toEqual("uuid1")
  })

  it("collectionName should be correct", () => {
    const curPageCount = 1  // Loaded by ngOnInit
    expect(component.collectionName).toEqual("my collection name")
  })

  it("loadSkillsInCollection should be correct", () => {
    // Arrange
    const expected = createMockPaginatedSkills()
    component.results = undefined

    // Act
    component.loadSkillsInCollection()
    while (!component.results) {}

    // Assert
    expect(component.results).toEqual(expected)
  })

  it("handleHeaderColumnSort should be correct", () => {
    // Arrange
    const expected = createMockPaginatedSkills()
    component.columnSort = ApiSortOrder.SkillAsc
    component.from = 1
    component.results = undefined

    // Act
    component.handleHeaderColumnSort(ApiSortOrder.NameDesc)
    while (!component.results) {}

    // Assert
    expect(component.columnSort).toEqual(ApiSortOrder.NameDesc)
    expect(component.from).toEqual(0)
    expect(component.results).toEqual(expected)
  })

  it("handlePageClicked should navigate", () => {
    // Arrange
    const expected = createMockPaginatedSkills()
    component.from = 111
    component.results = undefined

    // Act
    component.handlePageClicked(13)
    while (!component.results) {}

    // Assert
    expect(component.size).toEqual(50)
    expect(component.from).toEqual((13 - 1) * 50)
    expect(component.results).toEqual(expected)
  })
})
