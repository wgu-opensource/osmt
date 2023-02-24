import { ComponentFixture, TestBed } from "@angular/core/testing"

import { FilterChipsComponent } from "./filter-chips.component"
import {FormControl} from "@angular/forms"
import {ApiNamedReference} from "../../richskill/ApiSkill"

describe("FilterChipsComponent", () => {
  let component: FilterChipsComponent
  let fixture: ComponentFixture<FilterChipsComponent>
  const apiNameReferenced1 = new ApiNamedReference({id: "1", name: "value1"})
  const apiNameReferenced2 = new ApiNamedReference({id: "2", name: "value2"})

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
    component.control?.patchValue([
      apiNameReferenced1,
      apiNameReferenced2
    ])
    component.onRemoveChip(apiNameReferenced1)
    expect(component.control?.value.length).toBe(1)
  })
})
