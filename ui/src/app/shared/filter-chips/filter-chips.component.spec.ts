import { ComponentFixture, TestBed } from "@angular/core/testing"

import { FilterChipsComponent } from "./filter-chips.component"
import {FormControl} from "@angular/forms"

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
    component.control = new FormControl([])
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })

  it("remove chip should remove and element from chips", () => {
    component.control?.patchValue(["chip1", "chip2"])
    component.onRemoveChip("chip1")
    expect(component.control?.value.length).toBe(1)
  })
})
