import { async, ComponentFixture, TestBed } from "@angular/core/testing"

import { RichSkillFormComponent } from "./rich-skill-form.component"

describe("RichSkillFormComponent", () => {
  let component: RichSkillFormComponent
  let fixture: ComponentFixture<RichSkillFormComponent>

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RichSkillFormComponent ]
    })
    .compileComponents()
  }))

  beforeEach(() => {
    fixture = TestBed.createComponent(RichSkillFormComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
