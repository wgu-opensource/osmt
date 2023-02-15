import { ComponentFixture, TestBed } from "@angular/core/testing"

import { SearchMultiSelectComponent } from "./search-multi-select.component"
import {KeywordSearchService} from "../../richskill/service/keyword-search.service"
import {HttpClient, HttpClientModule} from "@angular/common/http"
import {AuthService} from "../../auth/auth-service"
import {AuthServiceStub} from "../../../../test/resource/mock-stubs"
import {RouterTestingModule} from "@angular/router/testing"
import {FormControl} from "@angular/forms"

describe("SearchMultiSelectComponent", () => {
  let component: SearchMultiSelectComponent
  let fixture: ComponentFixture<SearchMultiSelectComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SearchMultiSelectComponent ],
      providers: [
        KeywordSearchService,
        {provide: AuthService, useClass: AuthServiceStub},
      ],
      imports: [
        HttpClientModule,
        RouterTestingModule
      ]
    })
    .compileComponents()
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
})
