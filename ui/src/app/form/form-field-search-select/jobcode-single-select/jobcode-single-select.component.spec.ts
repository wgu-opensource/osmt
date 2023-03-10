import { ComponentFixture, TestBed } from "@angular/core/testing"

import { JobcodeSingleSelectComponent } from "./jobcode-single-select.component"
import {HttpClientTestingModule} from "@angular/common/http/testing"
import {AuthService} from "../../../auth/auth-service"
import {AuthServiceStub, KeywordSearchServiceStub} from "../../../../../test/resource/mock-stubs"
import {AppConfig} from "../../../app.config"
import {FormModule} from "../../form.module"
import {KeywordSearchService} from "../../../richskill/service/keyword-search.service"

describe("JobcodeSingleSelectComponent", () => {
  let component: JobcodeSingleSelectComponent
  let fixture: ComponentFixture<JobcodeSingleSelectComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobcodeSingleSelectComponent ],
      imports: [
        HttpClientTestingModule,
        FormModule
      ],
      providers: [
        AppConfig,
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: KeywordSearchService, useClass: KeywordSearchServiceStub },
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(JobcodeSingleSelectComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })

  it("performSearch should work", () => {
    const service = TestBed.inject(KeywordSearchService)
    const spy = spyOn(service, "searchJobcodes").and.callThrough()
    component.performSearch("jobcode 1")
    expect(spy).toHaveBeenCalled()
  })

  it("performSearch should not be called", () => {
    const service = TestBed.inject(KeywordSearchService)
    const spy = spyOn(service, "searchJobcodes").and.callThrough()
    component.performSearch("")
    expect(spy).not.toHaveBeenCalled()
  })
})
