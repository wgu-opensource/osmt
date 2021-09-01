import { Location } from "@angular/common"
import { HttpClientTestingModule } from "@angular/common/http/testing"
import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { Title } from "@angular/platform-browser"
import { ActivatedRoute, Router } from "@angular/router"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import { createMockCollection, createMockPaginatedSkills } from "../../../../test/resource/mock-data"
import { CollectionServiceStub, RichSkillServiceStub } from "../../../../test/resource/mock-stubs"
import { AppConfig } from "../../app.config"
import { EnvironmentService } from "../../core/environment.service"
import { PublishStatus } from "../../PublishStatus"
import { RichSkillService } from "../../richskill/service/rich-skill.service"
import { ToastService } from "../../toast/toast.service"
import { ApiCollection } from "../ApiCollection"
import { CollectionService } from "../service/collection.service"
import { PublishCollectionComponent } from "./publish-collection.component"


export function createComponent(T: Type<PublishCollectionComponent>): Promise<void> {
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
let component: PublishCollectionComponent
let fixture: ComponentFixture<PublishCollectionComponent>


describe("PublishCollectionComponent", () => {
  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        PublishCollectionComponent
      ],
      imports: [
        HttpClientTestingModule
      ],
      providers: [
        AppConfig,
        EnvironmentService,
        Title,
        Location,
        ToastService,
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useValue: routerSpy },
        { provide: RichSkillService, useClass: RichSkillServiceStub },
        { provide: CollectionService, useClass: CollectionServiceStub },
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()

    activatedRoute.setParamMap({ uuid: "uuid1" })
    createComponent(PublishCollectionComponent)
  }))

  it("should be created", () => {
    const collection = new ApiCollection(createMockCollection(
          new Date("2020-06-25T14:58:46.313Z"),
          new Date("2020-06-25T14:58:46.313Z"),
          new Date("2020-06-25T14:58:46.313Z"),
          new Date("2020-06-25T14:58:46.313Z"),
          PublishStatus.Draft
        ))

    expect(component).toBeTruthy()
    expect(component.uuidParam).toEqual("uuid1")
    expect(component.collectionLoaded).toBeTruthy()
    expect(component.collection).toEqual(collection)
    expect(component.activeState).toEqual(1)  // Got advanced by nextState()
    // Will test nextState() below
  })

  it("checkingDraft should be correct", () => {
    [
      { checkingDraft: false },
      { checkingDraft: false },
      { checkingDraft: true },
      { checkingDraft: false },
      { checkingDraft: false },
    ].forEach((param, index) => {
      component.activeState = index
      expect(component.checkingDraft).toEqual(param.checkingDraft)
    })
  })

  it("checkingArchived should be correct", () => {
    [
      { checkingArchived: false },
      { checkingArchived: true },
      { checkingArchived: false },
      { checkingArchived: false },
      { checkingArchived: false },
    ].forEach((param, index) => {
      component.activeState = index
      expect(component.checkingArchived).toEqual(param.checkingArchived)
    })
  })

  it("nextState should be correct", () => {
    expect(component.activeState).toEqual(1)
    component.nextState()
    expect(component.activeState).toEqual(2)
    component.nextState()
    expect(component.activeState).toEqual(3)
    component.nextState()
    expect(component.activeState).toEqual(4)
  })

  it("verb should be correct", () => {
    expect(component.activeState).toEqual(1)
    expect(component.verb).toEqual("archived")
    component.nextState()
    expect(component.verb).toEqual("draft")
  })

  it("checkForStatus should be correct", () => {
    // Arrange
    const expected = createMockPaginatedSkills()
    component.blockingSkills = undefined

    // Act
    component.checkForStatus(new Set([PublishStatus.Draft]))
    while (!component.blockingSkills) { }  // wait

    // Assert
    expect(component.blockingSkills).toEqual(expected)
  })

  it("handleClickCancel should cancel", () => {
    expect(component.handleClickCancel()).toBeFalse()
  })

  it("handleClickConfirmRemove should be correct", () => {
    // Arrange
    component.activeState = 1
    component.skillsSaved = undefined

    // Act
    expect(component.handleClickConfirmRemove()).toBeFalse()
    while (component.activeState === 1) { }  // wait

    // Assert
    expect(component.skillsSaved).toBeTruthy()
    expect(component.activeState).toEqual(2)
  })

  it("handleClickConfirmUnarchive should be correct", () => {
    expect(component.handleClickConfirmUnarchive()).toBeFalse()
  })

  it("handleClickConfirmPublish should be correct", () => {
    // Arrange
    component.activeState = 1
    component.skillsSaved = undefined

    // Act
    expect(component.handleClickConfirmPublish()).toBeFalse()
    while (component.activeState === 1) { }  // wait

    // Assert
    expect(component.skillsSaved).toBeTruthy()
    expect(component.activeState).toEqual(2)
  })
})
