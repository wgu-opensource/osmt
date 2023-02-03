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

describe("MyWorkspaceComponent", () => {
  let component: MyWorkspaceComponent
  let fixture: ComponentFixture<MyWorkspaceComponent>
  let collectionService: CollectionService

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([
          {
            path: "collections/uuid1/manage",
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
    const spy = spyOn(collectionService, "createCollection").and.callThrough()
    const spyNavigate = spyOn(router, "navigate").and.callThrough()
    component["convertToCollectionAction"]()
    expect(spy).toHaveBeenCalled()
    expect(spyNavigate).toHaveBeenCalled()
  })

})
