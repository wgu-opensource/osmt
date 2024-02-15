import { ComponentFixture, TestBed } from "@angular/core/testing"

import { HorizontalActionBarItemComponent } from "./horizontal-action-bar-item.component"

describe("HorizontalActionBarItemComponent", () => {
  let component: HorizontalActionBarItemComponent
  let fixture: ComponentFixture<HorizontalActionBarItemComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HorizontalActionBarItemComponent ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(HorizontalActionBarItemComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
