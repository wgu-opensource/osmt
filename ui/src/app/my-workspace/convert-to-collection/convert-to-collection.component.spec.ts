import {ComponentFixture, TestBed} from "@angular/core/testing"

import {ConvertToCollectionComponent} from "./convert-to-collection.component"
import {RouterTestingModule} from "@angular/router/testing"
import {HttpClientTestingModule} from "@angular/common/http/testing"
import {AppConfig} from "../../app.config"
import {Location} from "@angular/common"
import {Title} from "@angular/platform-browser"
import {ToastService} from "../../toast/toast.service"
import {EnvironmentService} from "../../core/environment.service"
import {CollectionServiceStub, EnvironmentServiceStub} from "../../../../test/resource/mock-stubs"
import {CollectionService} from "../../collection/service/collection.service"
import {ActivatedRoute, Router} from "@angular/router"
import {ActivatedRouteStubSpec} from "../../../../test/util/activated-route-stub.spec"

describe("ConvertToCollectionComponent", () => {
  let component: ConvertToCollectionComponent
  let fixture: ComponentFixture<ConvertToCollectionComponent>
  let activatedRoute: ActivatedRouteStubSpec

  activatedRoute = new ActivatedRouteStubSpec()
  activatedRoute.setParams({ uuid: "uuid1" })

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ConvertToCollectionComponent],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        AppConfig,
        Location,
        Title,
        ToastService,
        {provide: EnvironmentService, useClass: EnvironmentServiceStub},
        {provide: CollectionService, useClass: CollectionServiceStub},
        { provide: ActivatedRoute, useValue: activatedRoute },
      ]
    }).compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()

    const environmentService = TestBed.inject(EnvironmentService)
    environmentService.environment.editableAuthor = true
    AppConfig.settings.editableAuthor = true  // Doubly sure
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(ConvertToCollectionComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })

  it("on submit should call create", () => {
    const spyUpdateObject = spyOn(component, "updateObject")
    const router = TestBed.inject(Router)
    const spyRouter = spyOn(router, "navigate").and.resolveTo(true)
    const service = TestBed.inject(CollectionService)
    const spyService = spyOn(service, "createCollection").and.callThrough()
    component.onSubmit()
    expect(spyUpdateObject).toHaveBeenCalled()
    expect(spyService).toHaveBeenCalled()
    expect(spyRouter).toHaveBeenCalled()
  })

  it("update object should has skills", () => {
    const spyLocalStorage = spyOn(localStorage, "getItem").and.returnValue("[423423, 234234]")
    const updateObject = component.updateObject()
    expect(spyLocalStorage).toHaveBeenCalled()
    expect(updateObject.skills).toBeTruthy()
  })
})
