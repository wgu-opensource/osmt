import { ComponentFixture, TestBed } from "@angular/core/testing"

import { SearchMultiSelectComponent } from "./search-multi-select.component"

describe("SearchMultiSelectComponent", () => {
  let component: SearchMultiSelectComponent
  let fixture: ComponentFixture<SearchMultiSelectComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SearchMultiSelectComponent ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchMultiSelectComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
