import { ComponentFixture, TestBed } from "@angular/core/testing"
import { JobCodeTableComponent } from "./job-code-table.component"

describe("JobCodeTableComponent", () => {
  let component: JobCodeTableComponent
  let fixture: ComponentFixture<JobCodeTableComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobCodeTableComponent ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(JobCodeTableComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
