import {ComponentFixture, TestBed} from "@angular/core/testing"
import {SkillTableComponent} from "./skill-table.component"
import {createMockSkillSummary} from "../../../../test/resource/mock-data"
import {PublishStatus} from "../../PublishStatus"

describe("SkillTableComponent", () => {

  let component: SkillTableComponent
  let fixture: ComponentFixture<SkillTableComponent>

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        SkillTableComponent
      ],
      providers: [],
      imports: []
    }).compileComponents()
  })

  beforeEach(() => {
    fixture = TestBed.createComponent(SkillTableComponent)
    component = fixture.componentInstance
    fixture.detectChanges()
  })

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("shift selection should work from start to end", () => {
    component.isShiftPressed = true
    const spyEmit = spyOn(component.rowSelected, "emit")
    const firstSelected = {id: "1", uuid: "1abd-3", status: PublishStatus.Published, skillName: "Name 1", skillStatement: "", category: "", keywords: [], occupations: []}
    const secondSelected = {id: "5", uuid: "5abd-3", status: PublishStatus.Published, skillName: "Name 1", skillStatement: "", category: "", keywords: [], occupations: []}
    const items = [
      firstSelected,
      {id: "2", uuid: "2abd-3", status: PublishStatus.Published, skillName: "Name 1", skillStatement: "", category: "", keywords: [], occupations: []},
      {id: "3", uuid: "3abd-3", status: PublishStatus.Published, skillName: "Name 1", skillStatement: "", category: "", keywords: [], occupations: []},
      {id: "4", uuid: "4abd-3", status: PublishStatus.Published, skillName: "Name 1", skillStatement: "", category: "", keywords: [], occupations: []},
      secondSelected
    ]
    component.items = items
    component.selectedItems = new Set([firstSelected])
    component.onRowToggle(secondSelected)
    expect(component.selectedItems.size).toBe(5)
    expect(spyEmit).toHaveBeenCalledWith(items)
  })

  it("shift selection should work from end to start", () => {
    component.isShiftPressed = true
    const spyEmit = spyOn(component.rowSelected, "emit")
    const firstSelected = {id: "1", uuid: "1abd-3", status: PublishStatus.Published, skillName: "Name 1", skillStatement: "", category: "", keywords: [], occupations: []}
    const secondSelected = {id: "5", uuid: "5abd-3", status: PublishStatus.Published, skillName: "Name 1", skillStatement: "", category: "", keywords: [], occupations: []}
    const items = [
      {id: "2", uuid: "2abd-3", status: PublishStatus.Published, skillName: "Name 1", skillStatement: "", category: "", keywords: [], occupations: []},
      secondSelected,
      {id: "3", uuid: "3abd-3", status: PublishStatus.Published, skillName: "Name 1", skillStatement: "", category: "", keywords: [], occupations: []},
      {id: "4", uuid: "4abd-3", status: PublishStatus.Published, skillName: "Name 1", skillStatement: "", category: "", keywords: [], occupations: []},
      firstSelected
    ]
    component.items = items
    component.selectedItems = new Set([firstSelected])
    component.onRowToggle(secondSelected)
    expect(component.selectedItems.size).toBe(4)
    expect(spyEmit).toHaveBeenCalledWith(Array.from(component.selectedItems))
  })
})
