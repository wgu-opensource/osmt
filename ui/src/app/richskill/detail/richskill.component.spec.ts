import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {RichskillComponent} from "./richskill.component"

describe("RichskillComponent", () => {
  let component: RichskillComponent
  let fixture: ComponentFixture<RichskillComponent>

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [RichskillComponent]
    })
      .compileComponents()
  }))

  beforeEach(() => {
    fixture = TestBed.createComponent(RichskillComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
