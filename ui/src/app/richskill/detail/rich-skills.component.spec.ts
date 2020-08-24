import {async, ComponentFixture, TestBed} from "@angular/core/testing"

import {RichSkillsComponent} from "./rich-skills.component"

describe("RichSkillsComponent", () => {
  let component: RichSkillsComponent
  let fixture: ComponentFixture<RichSkillsComponent>

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [RichSkillsComponent]
    })
      .compileComponents()
  }))

  beforeEach(() => {
    fixture = TestBed.createComponent(RichSkillsComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
