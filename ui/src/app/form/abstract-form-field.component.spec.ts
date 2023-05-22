// noinspection LocalVariableNamingConventionJS
import {HttpClientTestingModule} from "@angular/common/http/testing"
import {Component, Type} from "@angular/core"
import {FormControl, Validators} from "@angular/forms"
import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {FormsModule, ReactiveFormsModule} from "@angular/forms"
import {ActivatedRoute, Router} from "@angular/router"
import {RouterTestingModule} from "@angular/router/testing"
import {AppConfig} from "src/app/app.config"
import {EnvironmentService} from "src/app/core/environment.service"
import {ActivatedRouteStubSpec} from "test/util/activated-route-stub.spec"
import {KeywordSearchService} from "../richskill/service/keyword-search.service"
import {KeywordSearchServiceStub} from "../../../test/resource/mock-stubs"
import {AbstractFormField} from "./abstract-form-field.component"

@Component({
  template: ""
})
export abstract class TestHostComponent extends AbstractFormField<ITestValueType|null> {
  get emptyValue() { return null }

  execProtected = {
    setControlValue: (value: ITestValueType|null, emitEvent: boolean = true) => this.setControlValue(value, emitEvent),
    handleValueChange: (newValue: string) => this.handleValueChange(newValue),
    onValueChange: (newValue: string) => this.onValueChange(newValue)
  }
}

interface ITestValueType {
  value : string
}

let activatedRoute: ActivatedRouteStubSpec
let fixture: ComponentFixture<TestHostComponent>
let component: TestHostComponent
let testResult1: ITestValueType

function createComponent(T: Type<TestHostComponent>): void {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance
  fixture.detectChanges()
  fixture.whenStable().then(() => fixture.detectChanges())
}

describe("AbstractFormFieldComponent", () => {
  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        AbstractFormField,
        TestHostComponent
      ],
      imports: [
        FormsModule,  // Required for ([ngModel])
        ReactiveFormsModule,
        RouterTestingModule,  // Required for routerLink
        HttpClientTestingModule,  // Needed to avoid the toolName race condition below
      ],
      providers: [
        AppConfig,  // Needed to avoid the toolName race condition below
        EnvironmentService,  // Needed to avoid the toolName race condition below
        { provide: ActivatedRoute, useValue: activatedRoute },
        { provide: KeywordSearchService, useClass: KeywordSearchServiceStub },
        { provide: Router, useValue: routerSpy },
      ]
    }).compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName

    activatedRoute.setParams({ userId: 126 })

    testResult1 = { value: "abc-123" } as ITestValueType

    // @ts-ignore
    createComponent(TestHostComponent)
  }))

  it("should be created", () => {
      expect(component).toBeTruthy()
  })

  it("get isError should return correct value", () => {
    component.control = new FormControl("abc-123", Validators.required)
    component.controlValue = component.emptyValue
    expect(component.isError).toEqual(true)
    component.controlValue = testResult1
    expect(component.isError).toEqual(false)
  })

  it("get isRequired should return correct value", () => {
    expect(component.isRequired).toEqual(false)
    component.required = true
    expect(component.isRequired).toEqual(true)
  })

  it("get controlValue should return correct value", () => {
    expect(component.controlValue).toEqual(component.emptyValue)
    component.controlValue = testResult1
    expect(component.controlValue).toEqual(testResult1)
  })

  it("set controlValue should succeed", () => {
    expect(component.controlValue === component.emptyValue).toEqual(true)
    component.controlValue = testResult1
    expect(component.controlValue).toEqual(testResult1)
  })

  it("clearValue should succeed", () => {
    component.controlValue = testResult1
    expect(component.controlValue).toEqual(testResult1)
    component.clearValue()
    expect(component.controlValue === component.emptyValue).toEqual(true)
  })

  it("clearField should succeed", () => {
    component.controlValue = testResult1
    expect(component.controlValue).toEqual(testResult1)
    component.clearField()
    expect(component.controlValue === component.emptyValue).toEqual(true)
  })

  it("setControlValue should succeed", () => {
    component.execProtected.setControlValue(testResult1, true)
    expect(component.controlValue).toEqual(testResult1)
  })

  it("handleControlValue should succeed", () => {
    expect((() => {
      component.execProtected.handleValueChange(testResult1.value)
      return true
    })()).toEqual(true)
  })

  it("onValueChange should succeed", () => {
    expect((() => {
      component.execProtected.onValueChange(testResult1.value)
      return true
    })()).toEqual(true)
  })
})
