import {async, ComponentFixture, TestBed} from "@angular/core/testing"

import {RichSkillsComponent} from "./rich-skills.component"
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing'
import { AppConfig } from 'src/app/app.config'

describe("RichSkillsComponent", () => {
  let component: RichSkillsComponent
  let fixture: ComponentFixture<RichSkillsComponent>
  let httpMock: HttpTestingController

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [RichSkillsComponent],
      imports: [HttpClientTestingModule],

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
    const req = httpMock.expectOne(`${AppConfig.settings.baseApiUrl}/api/skills`)
    expect(req.request.method).toBe("GET")
    req.flush([])
    expect(component).toBeTruthy()
  })
})
