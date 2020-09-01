import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {RichSkillComponent} from "./rich-skill.component"

describe("RichskillComponent", () => {
  let component: RichSkillComponent
  let fixture: ComponentFixture<RichSkillComponent>

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [RichSkillComponent]
    })
      .compileComponents()
  }))

  beforeEach(() => {
    fixture = TestBed.createComponent(RichSkillComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
