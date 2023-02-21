import {ComponentFixture, TestBed} from "@angular/core/testing"

import {SearchMultiSelectComponent} from "./search-multi-select.component"
import {KeywordSearchService} from "../../richskill/service/keyword-search.service"
import {AuthService} from "../../auth/auth-service"
import {AuthServiceStub} from "../../../../test/resource/mock-stubs"
import {RouterTestingModule} from "@angular/router/testing"
import {FormControl, ReactiveFormsModule} from "@angular/forms"
import {AppConfig} from "../../app.config"
import {createMockApiNamedReference, createMockJobcode} from "../../../../test/resource/mock-data"
import {of} from "rxjs"
import {KeywordType} from "../../richskill/ApiSkill"
import {HttpClientTestingModule} from "@angular/common/http/testing"

describe("SearchMultiSelectComponent", () => {
  let component: SearchMultiSelectComponent
  let fixture: ComponentFixture<SearchMultiSelectComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SearchMultiSelectComponent],
      providers: [
        AppConfig,
        KeywordSearchService,
        {provide: AuthService, useClass: AuthServiceStub},
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        ReactiveFormsModule
      ],
    }).compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchMultiSelectComponent)
    component = fixture.componentInstance
    component.control = new FormControl("")
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })

  it("show result should be true", () => {
    component.inputFc.patchValue("value")
    expect(component.showResults).toBeTrue()
  })

  it("clear field should clean input control", () => {
    component.inputFc.patchValue("value")
    component.clearField()
    expect(component.inputFc.value.length).toBe(0)
  })

  it("is result selected should be true", () => {
    component.control?.patchValue(["value1", "value2"])
    const isSelected = component.isResultSelected("value1")
    expect(isSelected).toBeTrue()
  })

  it("is result selected should be false", () => {
    component.control?.patchValue(["value1", "value2"])
    const isSelected = component.isResultSelected("value3")
    expect(isSelected).toBeFalse()
  })

  it("getKeywords should call search keywords", () => {
    const service = TestBed.inject(KeywordSearchService)
    const spy = spyOn(service, "searchKeywords").and.returnValue(of([createMockApiNamedReference("1", "value")]))
    component.keywordType = KeywordType.Keyword
    component["getKeywords"]("value")
    expect(spy).toHaveBeenCalled()
  })

  it("getKeywords should call search job codes", () => {
    const service = TestBed.inject(KeywordSearchService)
    const spy = spyOn(service, "searchJobcodes").and.returnValue(of([createMockJobcode()]))
    component["getKeywords"]("value")
    expect(spy).toHaveBeenCalled()
  })

  it("select result should add value in internal result", () => {
    component.control?.patchValue(["value1"])
    component.selectResult("value2")
    expect(component.control?.value?.length).toBe(2)
  })

  it("select result should remove value in internal result", () => {
    component.control?.patchValue(["value1", "value2"])
    component.selectResult("value2")
    expect(component.control?.value?.length).toBe(1)
  })
})
