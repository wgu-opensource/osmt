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
import {AbstractFormFieldSearchSelect} from "./abstract-form-field-search-select.component"

@Component({
  template: ""
})
export abstract class TestHostComponent extends AbstractFormFieldSearchSelect<ITestValueType, ITestValueType> {
  constructor(searchService: KeywordSearchService) { super(searchService) }
  areSearchResultsEqual(result1: ITestValueType, result2: ITestValueType): boolean { return result1?.value === result2?.value }
  isSearchResultType(result: ITestValueType): result is ITestValueType { return true }
  labelFor(result: ITestValueType): string|null { return result?.value ?? null }
  protected callSearchService(text: string): Observable<ITestValueType[]> { return of([{ value: "abc-123" } as ITestValueType]) }
  protected handleClearSearchClicked(): void { return }
  protected handleSearchValueChange(newValue: string): void { return }
  protected searchResultFromString(value: string): ITestValueType|undefined { return { value: value } as ITestValueType }

  execProtected = {
    setSearchControlValue: (value: string|null, emitEvent: boolean = true) => this.setSearchControlValue(value, emitEvent),
    performSearch: (text: string) => this.performSearch(text),
    selectSearchValue: () => this.selectSearchValue(),
    onSearchValueChange: (newValue: string) => this.onSearchValueChange(newValue),
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

describe("AbstractFormFieldSearchSelectComponent", () => {
  beforeEach(() => {
    activatedRoute = new ActivatedRouteStubSpec()
  })

  beforeEach(async(() => {
    const routerSpy = ActivatedRouteStubSpec.createRouterSpy()

    TestBed.configureTestingModule({
      declarations: [
        AbstractFormFieldSearchSelect,
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

  it("get isSearchDirty should return correct result", () => {
    expect(component.isSearchDirty).toEqual(false)
    component.searchControlValue =testResult1.value
    expect(component.isSearchDirty).toEqual(true)
  })

  it("get isSearchEmpty should return correct result", () => {
    expect(component.isSearchEmpty).toBeTruthy()
    component.searchControlValue = testResult1.value
    expect(component.isSearchEmpty).toBeFalsy()
  })

  it("get searchControlValue should return corrrect result", () => {
    expect(component.searchControlValue).toEqual("")
    component.searchControlValue = testResult1.value
    expect(component.searchControlValue).toEqual(testResult1.value)
  })

  it("get searchControlResultValue should return correct result", () => {
    expect(component.searchControlResultValue).toEqual(undefined)
    component.searchControlValue = testResult1.value
    component.results = [testResult1]
    expect(component.searchControlResultValue).toEqual(testResult1)
  })

  it("get showResults should return correct result", () => {
    expect(component.showResults).toEqual(false)
    component.searchControlValue = testResult1.value
    component.results = [testResult1]
    expect(component.showResults).toEqual(true)
  })

  it("clearSearch should succeed", () => {
    component.searchControlValue = testResult1.value
    component.results = [testResult1]
    component.clearSearch()
    expect(component.searchControlValue).toEqual("")
    expect(component.results === undefined).toEqual(true)
  })

  it("clearSearchResults should succeed", () => {
    component.results = [testResult1]
    component.clearSearchResults()
    expect(component.results === undefined).toEqual(true)
  })

  it("makeHtmlId should return correct result", () => {
    expect(component.makeHtmlId(testResult1)).toEqual("abc123")
  })

  it("performSearch not return", () => {
    component.execProtected.performSearch(testResult1.value)
    expect(component.results?.length).toEqual(1)
  })

  it("selectSearchValue should succeed", () => {
    expect((() => {
      component.execProtected.selectSearchValue()
      return true
    })()).toEqual(true)
  })

  it("onClearSearchClicked should succeed", () => {
    expect((() => {
      component.onClearSearchClicked()
      return true
    })()).toEqual(true)
  })

  it("onEnterKeyDown should succeed", () => {
    expect((() => {
      component.onEnterKeyDown(null)
      return true
    })()).toEqual(true)
  })

  it("onEnterKeyDown should succeed", () => {
    expect((() => {
      component.onEnterKeyDown(null)
      return true
    })()).toEqual(true)
  })

  it("handleSearchValueChange should succeed", () => {
    expect((() => {
      component.execProtected.handleSearchValueChange(testResult1.value)
      return true
    })()).toEqual(true)
  })
})
