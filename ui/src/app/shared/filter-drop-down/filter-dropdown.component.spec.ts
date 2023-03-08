import { ComponentFixture, TestBed } from "@angular/core/testing"

import { FilterDropdownComponent } from "./filter-dropdown.component"
import {FilterControlsComponent} from "../../table/filter-controls/filter-controls.component"
import {ReactiveFormsModule} from "@angular/forms"

describe("FilterComponent", () => {
  let component: FilterDropdownComponent
  let fixture: ComponentFixture<FilterDropdownComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        FilterDropdownComponent,
        FilterControlsComponent
      ],
      imports: [
        ReactiveFormsModule
      ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(FilterDropdownComponent)
    component = fixture.componentInstance
    component.filterFg = TestBed.createComponent(FilterControlsComponent).componentInstance["configureFilterFg"]()
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
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
