import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {APP_INITIALIZER} from "@angular/core"
import {initializeApp} from "../../app.module"
import {RichSkillsComponent} from "./rich-skills.component"
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing'
import { AppConfig } from 'src/app/app.config'
import { DefaultAppConfig } from 'src/app/models/app-config.model'
import { environment } from 'src/environments/environment'

describe("RichSkillsComponent", () => {
  let component: RichSkillsComponent
  let fixture: ComponentFixture<RichSkillsComponent>
  let httpMock: HttpTestingController

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [RichSkillsComponent],
      imports: [HttpClientTestingModule],
      providers: [
        AppConfig,
        { provide: APP_INITIALIZER,
          useFactory: initializeApp,
          deps: [AppConfig], multi: true }
        ]
      })
      .compileComponents()

      httpMock = TestBed.inject(HttpTestingController)
  }))

  beforeEach(() => {
    fixture = TestBed.createComponent(RichSkillsComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })


  afterEach(() => {
    httpMock.verify()
  })

  it("should create", () => {
    const req = httpMock.expectOne(`${environment.baseApiUrl}/api/skills`)
    expect(req.request.method).toBe("GET")
    req.flush([])
    expect(component).toBeTruthy()
  })
})
