import { ComponentFixture, TestBed } from "@angular/core/testing"

import { FilterChipsComponent } from "./filter-chips.component"

describe("FilterChipsComponent", () => {
  let component: FilterChipsComponent
  let fixture: ComponentFixture<FilterChipsComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FilterChipsComponent ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(FilterChipsComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })

  it("remove chip should remove and element from chips", () => {
    component.keywords = ["chip1", "chip2"]
    component.onRemoveChip("chip1")
    expect(component.keywords.length).toBe(1)
  })
})
