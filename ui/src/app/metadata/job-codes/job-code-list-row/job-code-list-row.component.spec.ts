import { ComponentFixture, TestBed } from "@angular/core/testing"
import { JobCodeListRowComponent } from "./job-code-list-row.component"

describe("JobCodeListRowComponent", () => {
  let component: JobCodeListRowComponent
  let fixture: ComponentFixture<JobCodeListRowComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobCodeListRowComponent ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(JobCodeListRowComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })
})
