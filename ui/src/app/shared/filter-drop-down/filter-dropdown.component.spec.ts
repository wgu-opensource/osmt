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
        FormsModule,
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
})
