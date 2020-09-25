import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {RichSkillComponent} from "./rich-skill.component"
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing"
import { ActivatedRoute, RouterModule } from '@angular/router'

describe("RichskillComponent", () => {
  let component: RichSkillComponent
  let fixture: ComponentFixture<RichSkillComponent>
  let httpMock: HttpTestingController

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [RichSkillComponent],
      imports: [HttpClientTestingModule, RouterModule.forRoot([]),],
    })
      .compileComponents()

      httpMock = TestBed.inject(HttpTestingController)
  }))

  beforeEach(() => {
    fixture = TestBed.createComponent(RichSkillComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  afterEach(() => {
    httpMock.verify()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
