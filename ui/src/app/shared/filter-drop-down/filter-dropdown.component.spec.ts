import { ComponentFixture, TestBed } from "@angular/core/testing"

import { FilterDropdownComponent } from "./filter-dropdown.component"
import {FormsModule, ReactiveFormsModule} from "@angular/forms"

describe("FilterComponent", () => {
  let component: FilterDropdownComponent
  let fixture: ComponentFixture<FilterDropdownComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FilterDropdownComponent ],
      imports: [
        ReactiveFormsModule
      ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(FilterDropdownComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })

  it("configure filter should works", () => {
    component["configureFilterFg"]()
    const value = component.filterFg.value
    const properties = ["categories", "keywords", "standards", "certifications", "occupations", "employers"]
    expect(value).toBeTruthy()
    expect(properties.every(p => p in value)).toBeTrue()
  })

  it("showInput should change", () => {
    const showInput = component.showInputs
    component.onApplyFilter()
    expect(component.showInputs !== showInput).toBeTrue()
  })

  it("on apply filter should call emit", () => {
    const spy = spyOn(component.applyFilter, "emit")
    component.onApplyFilter()
    expect(spy).toHaveBeenCalled()
  })
})
