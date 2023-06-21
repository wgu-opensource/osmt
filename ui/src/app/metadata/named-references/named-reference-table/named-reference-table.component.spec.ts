import { ComponentFixture, TestBed } from "@angular/core/testing"

import { NamedReferenceTableComponent } from "./named-reference-table.component"
import {ApiSortOrder} from "../../../richskill/ApiSkill";

describe("NamedReferenceTableComponent", () => {
  let component: NamedReferenceTableComponent
  let fixture: ComponentFixture<NamedReferenceTableComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NamedReferenceTableComponent ]
    })
    .compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(NamedReferenceTableComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should create", () => {
    expect(component).toBeTruthy()
  })

  it("Current sort should be name.asc", () => {
    component.sortColumn("name", true)
    expect(component.currentSort).toEqual(ApiSortOrder.KeywordNameAsc)
  })

  it("Current sort should be name.desc", () => {
    component.sortColumn("name", false)
    expect(component.currentSort).toEqual(ApiSortOrder.KeywordNameDesc)
  })

  it("Current sort should be code.asc", () => {
    component.sortColumn("framework", true)
    expect(component.currentSort).toEqual(ApiSortOrder.KeywordFrameworkAsc)
  })

  it("Current sort should be code.desc", () => {
    component.sortColumn("framework", false)
    expect(component.currentSort).toEqual(ApiSortOrder.KeywordFrameworkDesc)
  })

  it("Get code sort should be true", () => {
    component.currentSort = ApiSortOrder.KeywordFrameworkAsc
    const codeSort = component.getFrameworkSort()
    expect(codeSort).toBeTrue()
  })
})
