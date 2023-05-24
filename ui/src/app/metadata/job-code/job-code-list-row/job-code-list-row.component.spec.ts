import { ComponentFixture, TestBed } from "@angular/core/testing"
import { JobCodeListRowComponent } from "./job-code-list-row.component"
import { mockJobCodeWithParents } from "@test/resource/mock-data"

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

  it("Parents should be ordered", () => {
    component.jobCode = mockJobCodeWithParents
    const sorted = component.sortedParents()
    expect(sorted[0].code).toBe("13-0000")
    expect(sorted[1].code).toBe("13-2000")
    expect(sorted[2].code).toBe("13-2010")
  })
})
