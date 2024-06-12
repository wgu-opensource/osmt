// noinspection LocalVariableNamingConventionJS
import {HttpClientTestingModule} from "@angular/common/http/testing"
import {Component, Type} from "@angular/core"
import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {FormsModule, ReactiveFormsModule} from "@angular/forms"
import {ActivatedRoute, Router} from "@angular/router"
import {RouterTestingModule} from "@angular/router/testing"
import {Observable, of} from "rxjs"
import {AppConfig} from "src/app/app.config"
import {EnvironmentService} from "src/app/core/environment.service"
import {ActivatedRouteStubSpec} from "test/util/activated-route-stub.spec"
import {KeywordSearchService} from "../../richskill/service/keyword-search.service"
import {KeywordSearchServiceStub} from "../../../../test/resource/mock-stubs"
import {AbstractFormFieldSearchSingleSelect} from "./abstract-form-field-search-single-select.component"

@Component({
  template: ""
})
export abstract class TestHostComponent extends AbstractFormFieldSearchSingleSelect<ITestValueType> {
  constructor(searchService: KeywordSearchService) { super(searchService) }
  areSearchResultsEqual(result1: ITestValueType, result2: ITestValueType): boolean { return result1?.value === result2?.value }
  isSearchResultType(result: ITestValueType): result is ITestValueType { return true }
  labelFor(result: ITestValueType): string|null { return result?.value ?? null }
  protected callSearchService(text: string): Observable<ITestValueType[]> { return of([]) }
  protected searchResultFromString(value: string): ITestValueType|undefined { return { value: value } as ITestValueType }

  execProtected = {
    setSearchToValue: () => this.setSearchToValue(),
    handleClearSearchClicked: () => this.handleClearSearchClicked(),
    handleSearchResultClicked: (result: ITestValueType) => this.handleSearchResultClicked(result),
    handleSearchValueChange: (newValue: string) => this.handleSearchValueChange(newValue)
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

describe("AbstractFormFieldSearchSingleSelectComponent", () => {
  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        AbstractFormFieldSearchSingleSelect,
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

  it("isResultSelected should return correct result", () => {
    expect(component.isResultSelected(testResult1)).toEqual(false)
    component.selectResult(testResult1)
    expect(component.isResultSelected(testResult1)).toEqual(true)
  })

  it("selectResult should succeed", () => {
    expect(component.isResultSelected(testResult1)).toEqual(false)
    component.selectResult(testResult1)
    expect(component.isResultSelected(testResult1)).toEqual(true)
  })

  it("setSearchToValue should succeed", () => {
    component.searchControlValue = ""
    component.selectResult(testResult1)
    component.execProtected.setSearchToValue()
    expect(component.searchControlValue).toEqual(testResult1.value)
  })

  it("handleClearSearchClicked should succeed", () => {
    component.selectResult(testResult1)
    component.execProtected.handleClearSearchClicked()
    expect(component.controlValue === null).toEqual(true)
  })

  it("handleSearchResultClicked should succeed", () => {
    expect((() => {
      component.execProtected.handleSearchResultClicked(testResult1)
      return true
    })()).toEqual(true)
  })

  it("handleSearchValueChange should succeed", () => {
    expect((() => {
      component.execProtected.handleSearchValueChange(testResult1.value)
      return true
    })()).toEqual(true)
  })

  it("exact match detection works", () => {
    component.results = []
    component.searchControlValue = "abc";
    expect(component.isExactMatchFound()).toEqual(false)

    component.results = [{value:'abc'}]
    expect(component.isExactMatchFound()).toEqual(true)
  })
})
