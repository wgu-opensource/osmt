import {FilterControlsComponent} from "./filter-controls.component"
import {ComponentFixture, TestBed} from "@angular/core/testing"

describe("FilterControlsComponent", () => {
  let component: FilterControlsComponent
  let fixture: ComponentFixture<FilterControlsComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FilterControlsComponent],
      imports: [],
      providers: []
    }).compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(FilterControlsComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("configure filter should works", () => {
    component["configureFilterFg"]()
    const value = component.filterFg.value
    const properties = ["categories", "keywords", "standards", "certifications", "occupations", "employers"]
    expect(value).toBeTruthy()
    expect(properties.every(p => p in value)).toBeTrue()
  })

})
