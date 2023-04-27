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
import {AbstractFormFieldSearchMultiSelect} from "./abstract-form-field-search-multi-select.component"

@Component({
  template: ""
})
export abstract class TestHostComponent extends AbstractFormFieldSearchMultiSelect<ITestValueType> {
  constructor(searchService: KeywordSearchService) { super(searchService) }
  areSearchResultsEqual(result1: ITestValueType, result2: ITestValueType): boolean { return true }
  isSearchResultType(result: ITestValueType): result is ITestValueType { return true }
  labelFor(result: ITestValueType): string|null { return result.value }
  protected callSearchService(text: string): Observable<ITestValueType[]> { return of([]) }
  protected searchResultFromString(value: string): ITestValueType|undefined { return { value: value } as ITestValueType }

  execProtected = {
    handleClearSearchClicked: () => this.handleClearSearchClicked(),
    handleSearchResultClicked: (result: ITestValueType) => this.handleSearchResultClicked(result),
    handleSelectedResultClicked: (result: ITestValueType) => this.handleSelectedResultClicked(result)
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

describe("AbstractFormFieldSearchMultiSelectComponent", () => {
  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        AbstractFormFieldSearchMultiSelect,
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

  it("get selectedResults should return correct result", () => {
    expect(component.selectedResults).toEqual([])
    component.selectResult(testResult1)
    expect(component.selectedResults).toEqual([testResult1])
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

  it("unselectResult should succeed", () => {
    expect(component.isResultSelected(testResult1)).toEqual(false)
    component.selectResult(testResult1)
    expect(component.isResultSelected(testResult1)).toEqual(true)
    component.unselectResult(testResult1)
    expect(component.isResultSelected(testResult1)).toEqual(false)
  })

  it("onSelectedResultClicked should succeed", () => {
    expect((() => {
      component.onSelectedResultClicked(testResult1)
      return true
    })()).toEqual(true)
  })

  it("handleClearSearchClicked should succeed", () => {
    expect((() => {
      component.execProtected.handleClearSearchClicked()
      return true
    })()).toEqual(true)
  })

  it("handleSearchResultClicked should succeed", () => {
    expect((() => {
      component.execProtected.handleSearchResultClicked(testResult1)
      return true
    })()).toEqual(true)
  })

  it("handleSelectedResultClicked should succeed", () => {
    expect((() => {
      component.execProtected.handleSelectedResultClicked(testResult1)
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
