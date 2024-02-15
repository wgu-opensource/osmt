import {ComponentFixture, TestBed} from "@angular/core/testing"
import {SkillTableComponent} from "./skill-table.component"
import {createMockSkillSummary} from "../../../../test/resource/mock-data"
import {PublishStatus} from "../../PublishStatus"
import {ApiSkillSummary, ISkillSummary} from "../../richskill/ApiSkillSummary"
import {IJobCode} from "../../job-codes/Jobcode"

describe("SkillTableComponent", () => {

  let component: SkillTableComponent
  let fixture: ComponentFixture<SkillTableComponent>
  const keywords = ["keyword 1", "keyword 2"]
  const occupations: IJobCode[] = [{code: "Code 1"}]

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
    createMockSkillSummary()
    const spyEmit = spyOn(component.rowSelected, "emit")
    const firstSelected = createMockSkillSummary("1", PublishStatus.Published)
    const secondSelected = createMockSkillSummary("5", PublishStatus.Published)
    const items: ApiSkillSummary[] = [
      firstSelected,
      createMockSkillSummary("1", PublishStatus.Published),
      createMockSkillSummary("3", PublishStatus.Published),
      createMockSkillSummary("3", PublishStatus.Published),
      secondSelected
    ]
    component.items = items
    component.selectedItems = new Set<ApiSkillSummary>([firstSelected])
    component.onRowToggle(secondSelected)
    expect(component.selectedItems.size).toBe(5)
    expect(spyEmit).toHaveBeenCalledWith(items)
  })

  it("shift selection should work from end to start", () => {
    component.isShiftPressed = true
    const spyEmit = spyOn(component.rowSelected, "emit")
    const firstSelected = createMockSkillSummary("1", PublishStatus.Published)
    const secondSelected = createMockSkillSummary("5", PublishStatus.Published)
    const items = [
      createMockSkillSummary("2", PublishStatus.Published),
      secondSelected,
      createMockSkillSummary("3", PublishStatus.Published),
      createMockSkillSummary("4", PublishStatus.Published),
      firstSelected
    ]
    component.items = items
    component.selectedItems = new Set<ApiSkillSummary>([firstSelected])
    component.onRowToggle(secondSelected)
    expect(component.selectedItems.size).toBe(4)
    expect(spyEmit).toHaveBeenCalledWith(Array.from(component.selectedItems))
  })
})
