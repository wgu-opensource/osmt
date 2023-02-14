import { ComponentFixture, TestBed } from "@angular/core/testing"

import { FilterComponent } from "./filter.component"
import {FormsModule, ReactiveFormsModule} from "@angular/forms"

describe("FilterComponent", () => {
  let component: FilterComponent
  let fixture: ComponentFixture<FilterComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FilterComponent ],
      imports: [
        FormsModule,
        ReactiveFormsModule
      ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(FilterComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
