import { ComponentFixture, TestBed } from "@angular/core/testing"

import { VerticalActionBarItemComponent } from "./vertical-action-bar-item.component"

describe("ActionBarItemComponent", () => {
  let component: VerticalActionBarItemComponent
  let fixture: ComponentFixture<VerticalActionBarItemComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VerticalActionBarItemComponent ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(VerticalActionBarItemComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
