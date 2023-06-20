import { ComponentFixture, TestBed } from "@angular/core/testing"
import { JobCodeTableComponent } from "./job-code-table.component"
import { ApiSortOrder } from "../../../richskill/ApiSkill"
import { curry } from "lodash"

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

  it("Current sort should be name.asc", () => {
    component.sortColumn("name", true)
    expect(component.currentSort).toEqual(ApiSortOrder.NameAsc)
  })

  it("Current sort should be name.desc", () => {
    component.sortColumn("name", false)
    expect(component.currentSort).toEqual(ApiSortOrder.NameDesc)
  })

  it("Current sort should be code.asc", () => {
    component.sortColumn("code", true)
    expect(component.currentSort).toEqual(ApiSortOrder.CodeAsc)
  })

  it("Current sort should be code.desc", () => {
    component.sortColumn("code", false)
    expect(component.currentSort).toEqual(ApiSortOrder.CodeDesc)
  })

  it("Current sort should be jobCodeLevel.asc", () => {
    component.sortColumn("jobCodeLevel", true)
    expect(component.currentSort).toEqual(ApiSortOrder.JobCodeLevelAsc)
  })

  it("Current sort should be jobCodeLevel.desc", () => {
    component.sortColumn("jobCodeLevel", false)
    expect(component.currentSort).toEqual(ApiSortOrder.JobCodeLevelDesc)
  })

  it("Get code sort should be true", () => {
    component.currentSort = ApiSortOrder.CodeAsc
    const codeSort = component.getCodeSort()
    expect(codeSort).toBeTrue()
  })

  it("Get job code level should be true", () => {
    component.currentSort = ApiSortOrder.JobCodeLevelAsc
    const codeSort = component.getJobCodeLevelSort()
    expect(codeSort).toBeTrue()
  })
})
