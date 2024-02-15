// noinspection LocalVariableNamingConventionJS
import {HttpClientTestingModule} from "@angular/common/http/testing"
import {Component, Type} from "@angular/core"
import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {FormsModule, ReactiveFormsModule} from "@angular/forms"
import {ActivatedRoute, Router} from "@angular/router"
import {RouterTestingModule} from "@angular/router/testing"
import {AppConfig} from "src/app/app.config"
import {EnvironmentService} from "src/app/core/environment.service"
import {ActivatedRouteStubSpec} from "test/util/activated-route-stub.spec"
import {KeywordSearchService} from "../../../richskill/service/keyword-search.service"
import {KeywordSearchServiceStub} from "../../../../../test/resource/mock-stubs"
import {ApiJobCode, IJobCode} from "../../../job-codes/Jobcode"
import {FormFieldJobCodeSearchMultiSelect} from "./form-field-jobcode-search-multi-select.component"

@Component({
  template: "<app-form-field-jobcode-search-multi-select ></app-form-field-jobcode-search-multi-select>"
})
export abstract class TestHostComponent extends FormFieldJobCodeSearchMultiSelect {
  execProtected = {
    callSearchService: (text: string) => this.callSearchService(text)
  }
}

let activatedRoute: ActivatedRouteStubSpec
let fixture: ComponentFixture<TestHostComponent>
let component: TestHostComponent
let testResult1: IJobCode
let testResult2: IJobCode

function createComponent(T: Type<TestHostComponent>): void {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance
  fixture.detectChanges()
  fixture.whenStable().then(() => fixture.detectChanges())
}

describe("FormFieldJobCodeSearchMultiSelectComponent", () => {
  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        FormFieldJobCodeSearchMultiSelect,
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

    testResult1 = new ApiJobCode({ targetNodeName: "Job Code 1", code: "1.101" })
    testResult2 = new ApiJobCode({ targetNodeName: "Job Code 2", code: "1.102" })

    // @ts-ignore
    createComponent(TestHostComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("areSearchResultsEqual should return correct result", () => {
    expect(component.areSearchResultsEqual(testResult1, testResult1)).toEqual(true)
    expect(component.areSearchResultsEqual(testResult1, testResult2)).toEqual(false)
  })

  it("isSearchResultType should return correct result", () => {
    expect(component.isSearchResultType(testResult1)).toEqual(true)
    expect(component.isSearchResultType({})).toEqual(false)
  })

  it("labelFor should return correct result", () => {
    expect(component.labelFor(testResult1)).toEqual(`${testResult1.code}|${testResult1.targetNodeName}`)
  })

  it("searchResultFromString should return correct result", () => {
    expect(component.searchResultFromString(testResult1.targetNodeName!!)).toEqual(undefined)
  })

  it("callSearchService should succeed", () => {
    expect((() => {
      component.execProtected.callSearchService(testResult1.targetNodeName!!)
      return true
    })()).toEqual(true)
  })
})
