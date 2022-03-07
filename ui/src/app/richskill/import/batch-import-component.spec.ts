import { HttpClientModule } from "@angular/common/http"
import { Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { ActivatedRoute, Router } from "@angular/router"
import { RouterTestingModule } from "@angular/router/testing"
import { ActivatedRouteStubSpec } from "test/util/activated-route-stub.spec"
import { AuthServiceStub } from "../../../../test/resource/mock-stubs"
import { AppConfig } from "../../app.config"
import { AuthService } from "../../auth/auth-service"
import { EnvironmentService } from "../../core/environment.service"
import { BatchImportComponent } from "./batch-import.component"


export function createComponent(T: Type<BatchImportComponent>): Promise<void> {
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
let component: BatchImportComponent
let fixture: ComponentFixture<BatchImportComponent>


describe("BatchImportComponent", () => {
  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        BatchImportComponent
      ],
      imports: [
        // FormsModule,  // Required for ([ngModel])
        RouterTestingModule,  // Required for routerLink
        HttpClientModule
      ],
      providers: [
        AppConfig,
        EnvironmentService,
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: Router, useValue: routerSpy },
        { provide: AuthService, useClass: AuthServiceStub }
      ]
    })
      .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()

    createComponent(BatchImportComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("stepName should return proper step string", () => {
    expect(component.stepName(0)).toEqual("")
    expect(component.stepName(1)).toEqual("Select File")
    expect(component.stepName(2)).toEqual("Map Fields")
    expect(component.stepName(3)).toEqual("Review and Import")
    expect(component.stepName(4)).toEqual("Success!")
  })

  it("nextButtonLabel should return Next/Import correctly", () => {
    expect(component.nextButtonLabel).toEqual("Next")
    component.currentStep = 3
    expect(component.nextButtonLabel).toEqual("Import")
  })

  it("cancelButtonLabel should return Cancel Import/Cancel correctly", () => {
    expect(component.cancelButtonLabel).toEqual("Cancel")
    component.currentStep = 2
    expect(component.cancelButtonLabel).toEqual("Cancel Import")
  })

  it("recordCount should return correct count", () => {
    expect(component.recordCount).toEqual(0)
  })

  it("validCount should return correct count", () => {
    expect(component.validCount).toEqual(0)
  })

  it("handleClickNext should return false", () => {
    expect(component.handleClickNext()).toBeFalse()
    component.currentStep = 3
    expect(component.handleClickNext()).toBeFalse()
  })

  it("handleClickCancel should set current step to previous value", () => {
    expect(component.handleClickCancel()).toBeFalse()
    component.currentStep = 2
    component.handleClickCancel()
    expect(component.currentStep).toEqual(1)
  })
})
