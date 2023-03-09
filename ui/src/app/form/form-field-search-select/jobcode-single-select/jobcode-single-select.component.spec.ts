import { ComponentFixture, TestBed } from "@angular/core/testing"

import { JobcodeSingleSelectComponent } from "./jobcode-single-select.component"

describe("JobcodeSingleSelectComponent", () => {
  let component: JobcodeSingleSelectComponent
  let fixture: ComponentFixture<JobcodeSingleSelectComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobcodeSingleSelectComponent ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(JobcodeSingleSelectComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
