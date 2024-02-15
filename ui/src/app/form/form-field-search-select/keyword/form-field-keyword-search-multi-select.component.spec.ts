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
import {ApiNamedReference, INamedReference, KeywordType} from "../../../richskill/ApiSkill"
import {FormFieldKeywordSearchMultiSelect} from "./form-field-keyword-search-multi-select.component"

@Component({
  template: "<app-form-field-keyword-search-multi-select></app-form-field-keyword-search-multi-select>"
})
export abstract class TestHostComponent extends FormFieldKeywordSearchMultiSelect {
  keywordType = KeywordType.Certification
  createNonExisting = true

  execProtected = {
    callSearchService: (text: string) => this.callSearchService(text)
  }
}

let activatedRoute: ActivatedRouteStubSpec
let fixture: ComponentFixture<TestHostComponent>
let component: TestHostComponent
let testResult1: INamedReference
let testResult2: INamedReference

function createComponent(T: Type<TestHostComponent>): void {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance
  fixture.detectChanges()
  fixture.whenStable().then(() => fixture.detectChanges())
}

describe("FormFieldKeywordSearchMultiSelectComponent", () => {
  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        FormFieldKeywordSearchMultiSelect,
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

    testResult1 =  new ApiNamedReference({ name: "abc-123" })
    testResult2 = new ApiNamedReference({ name: "321-cba" })

    // @ts-ignore
    createComponent(TestHostComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("get isAlignmentKeywordType should return correct result", () => {
    expect(component.isAlignmentKeywordType).toEqual(false)
  })

  it("get isNamedReferenceKeywordType should return correct result", () => {
    expect(component.isNamedReferenceKeywordType).toEqual(true)
  })

  it("get isStringKeywordType should return correct result", () => {
    expect(component.isStringKeywordType).toEqual(false)
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
    expect(component.labelFor(testResult1)).toEqual(testResult1.name!!)
  })

  it("searchResultFromString should return correct result", () => {
    expect(component.searchResultFromString(testResult1.name!!)).toEqual(testResult1)
  })

  it("callSearchService should succeed", () => {
    expect((() => {
      component.execProtected.callSearchService(testResult1.name!!)
      return true
    })()).toEqual(true)
  })
})
