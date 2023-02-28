import { Location } from "@angular/common"
import { HttpClientTestingModule } from "@angular/common/http/testing"
import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { Title } from "@angular/platform-browser"
import { ActivatedRoute, Router } from "@angular/router"
import { RouterTestingModule } from "@angular/router/testing"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import { CollectionServiceStub, EnvironmentServiceStub } from "../../../../test/resource/mock-stubs"
import { AppConfig } from "../../app.config"
import { EnvironmentService } from "../../core/environment.service"
import { ApiNamedReference } from "../../richskill/ApiSkill"
import { ToastService } from "../../toast/toast.service"
import { CollectionService } from "../service/collection.service"
import { CollectionFormComponent } from "./collection-form.component"


export function createComponent(T: Type<CollectionFormComponent>): Promise<void> {
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
let component: CollectionFormComponent
let fixture: ComponentFixture<CollectionFormComponent>


describe("CollectionFormComponent", () => {
  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        CollectionFormComponent
      ],
      imports: [
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        AppConfig,
        Location,
        Title,
        ToastService,
        { provide: EnvironmentService, useClass: EnvironmentServiceStub },
        { provide: CollectionService, useClass: CollectionServiceStub },
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useValue: routerSpy },
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()

    const environmentService = TestBed.inject(EnvironmentService)
    environmentService.environment.editableAuthor = true
    AppConfig.settings.editableAuthor = true  // Doubly sure

    activatedRoute.setParams({ uuid: "uuid1" })
    createComponent(CollectionFormComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("should initialize", () => {
    expect(component.collectionUuid).toEqual("uuid1")
    expect(component.collectionForm.get("collectionName")?.value).toEqual("my collection name")
  })

  it("nameLabel should return", () => {
    component.collectionUuid = ""
    expect(component.nameLabel).toEqual("New Collection Name")

    component.collectionUuid = "uuid"
    expect(component.nameLabel).toEqual("Collection Name")
  })

  it("formGroup should return", () => {
    expect(component.formGroup()).toBeTruthy()
  })

  it("namedReferenceForString should be correct", () => {
    expect(component.namedReferenceForString("")).toEqual(undefined)
    expect(component.namedReferenceForString("a://b")).toEqual(new ApiNamedReference({ id: "a://b" }))
    expect(component.namedReferenceForString("abc")).toEqual(new ApiNamedReference({ name: "abc" }))
  })

  it("updateObject should return", () => {
    // Arrange
    const value = {
      collectionName: "collection1",
      description: "description1",
      author: "author1"
    }
    component.collectionForm.setValue(value)

    // Act
    const result = component.updateObject()

    // Assert
    expect(result.name).toEqual(value.collectionName)
    expect(result.description).toEqual(value.description)
    expect(result.author).toEqual(value.author)
  })

  it("onSubmit should be correct", (done) => {
    // Arrange
    const uuid = "uuid1"
    component.collectionUuid = uuid
    const value = {
      collectionName: "collection1",
      description: "description1",
      author: "author1"
    }
    component.collectionForm.setValue(value)
    const router = TestBed.inject(Router)

    // Act
    component.onSubmit()

    // Assert
    component.collectionSaved?.subscribe((collection) => {
      expect(collection.uuid).toEqual(uuid)
      expect(router.navigate).toHaveBeenCalledWith([ "/collections/uuid1/manage" ])
      done()
    })
  })
})
