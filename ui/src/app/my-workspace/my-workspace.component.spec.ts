import {ComponentFixture, TestBed} from "@angular/core/testing"

import {MyWorkspaceComponent} from "./my-workspace.component"
import {RouterTestingModule} from "@angular/router/testing"
import {AuthService} from "../auth/auth-service"
import {RichSkillService} from "../richskill/service/rich-skill.service"
import {AuthServiceStub, CollectionServiceStub, EnvironmentServiceStub, RichSkillServiceStub} from "../../../test/resource/mock-stubs"
import {HttpClientTestingModule} from "@angular/common/http/testing"
import {AppConfig} from "../app.config"
import {Title} from "@angular/platform-browser"
import {ToastService} from "../toast/toast.service"
import {EnvironmentService} from "../core/environment.service"
import {CollectionService} from "../collection/service/collection.service"
import {Router} from "@angular/router"
import {ManageCollectionComponent} from "../collection/detail/manage-collection.component"
import {createMockCollection, createMockSkillSummary} from "../../../test/resource/mock-data"
import {PublishStatus} from "../PublishStatus"

describe("MyWorkspaceComponent", () => {
  let component: MyWorkspaceComponent
  let fixture: ComponentFixture<MyWorkspaceComponent>
  let collectionService: CollectionService

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([
          {
            path: "my-workspace/uuid1/add-skills",
            component: ManageCollectionComponent
          }
        ]),
        HttpClientTestingModule,
      ],
      declarations: [MyWorkspaceComponent],
      providers: [
        AppConfig,
        Title,
        ToastService,
        {provide: EnvironmentService, useClass: EnvironmentServiceStub},  // Example of using a service stub
        {provide: RichSkillService, useClass: RichSkillServiceStub},
        {provide: CollectionService, useClass: CollectionServiceStub},
        {provide: AuthService, useClass: AuthServiceStub},
      ]
    }).compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(MyWorkspaceComponent)
    component = fixture.componentInstance
    collectionService  = TestBed.inject(CollectionService)
    fixture.detectChanges()
  })

  it("reload collection should call get workspace", () => {
    const spy = spyOn(collectionService, "getWorkspace").and.callThrough()
    component.reloadCollection()
    expect(spy).toHaveBeenCalled()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })

  it("actions definitions should be correct", () => {
    expect(component.actionDefinitions().length).toEqual(4)
  })

  it("handle confirm delete collection", () => {
    const spy = spyOn(component, "submitSkillRemoval").and.callThrough()
    component.handleConfirmDeleteCollection()
    expect(spy).toHaveBeenCalled()
    expect(component.template).toEqual("default")
  })

  it("convert to collection action", () => {
    const router = TestBed.inject(Router)
    const spyNavigate = spyOn(router, "navigate").and.callThrough()
    const spyLocalStorage = spyOn(localStorage, "setItem").and.callThrough()
    component["convertToCollectionAction"]()
    expect(spyLocalStorage).toHaveBeenCalled()
    expect(spyNavigate).toHaveBeenCalled()
  })

  it("confirm message text", () => {
    const date = new Date()
    component.collection = createMockCollection(date, date, date, date, PublishStatus.Workspace)
    expect(component.confirmMessageText).toBe("reset My Workspace")
  })

  it("confirm button text", () => {
    const date = new Date()
    component.collection = createMockCollection(date, date, date, date, PublishStatus.Workspace)
    expect(component.confirmButtonText).toBe("reset My Workspace")
  })

  it("workspace is not empty", () => {
    const date = new Date()
    component.collection = createMockCollection(date, date, date, date, PublishStatus.Workspace)
    expect(component["workspaceEmpty"]()).toBeFalse()
  })

  it("router should navigate correctly", () => {
    const router = TestBed.inject(Router)
    const spy = spyOn(router, "navigate").and.resolveTo(true)
    component.addSkillsAction()
    expect(spy).toHaveBeenCalledWith(["/my-workspace/uuid1/add-skills"])
  })

  it("add to collection should not be visible", () => {
    expect(component.addToCollectionVisible()).toBeFalse()
  })

  it("add to collection should be visible", () => {
    component.selectedSkills = [createMockSkillSummary()]
    expect(component.addToCollectionVisible()).toBeTrue()
  })

})
