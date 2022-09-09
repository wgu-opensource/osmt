// noinspection MagicNumberJS,LocalVariableNamingConventionJS

import { Component, ElementRef, Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { RouterTestingModule } from "@angular/router/testing"
import { createMockPaginatedSkills, createMockSkillSummary } from "../../../../test/resource/mock-data"
import {AuthServiceStub, RichSkillServiceStub} from "../../../../test/resource/mock-stubs"
import { PublishStatus } from "../../PublishStatus"
import { ToastService } from "../../toast/toast.service"
import { ApiSortOrder } from "../ApiSkill"
import { ApiSearch, PaginatedSkills } from "../service/rich-skill-search.service"
import { RichSkillService } from "../service/rich-skill.service"
import { SkillsListComponent } from "./skills-list.component"
import {AuthService} from "../../auth/auth-service";


@Component({
  selector: "app-concrete-component",
  template: ``
})
class ConcreteComponent extends SkillsListComponent {
  matchingQuery?: string[]
  title = "Concrete Skills"

  loadNextPage(): void {}

  handleSelectAll(selectAllChecked: boolean): void {}

  public setResults(results: PaginatedSkills): void {
    super.setResults(results)
  }
}


export function createComponent(T: Type<ConcreteComponent>): Promise<void> {
  fixture = TestBed.createComponent(T)
  component = fixture.componentInstance

  // 1st change detection triggers ngOnInit which gets a hero
  fixture.detectChanges()

  return fixture.whenStable().then(() => {
    // 2nd change detection displays the async-fetched hero
    fixture.detectChanges()
  })
}


let component: ConcreteComponent
let fixture: ComponentFixture<ConcreteComponent>


describe("SkillsListComponent", () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        ConcreteComponent
      ],
      imports: [
        RouterTestingModule.withRoutes([
          { path: "collections/add-skills", component: SkillsListComponent }
        ])
      ],
      providers: [
        ToastService,
        { provide: RichSkillService, useClass: RichSkillServiceStub },
        { provide: AuthService, useClass: AuthServiceStub },
      ]
    })

    createComponent(ConcreteComponent)
    component.titleElement = new ElementRef(document.getElementById("titleHeading"))
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("title should be correct", () => {
    expect(component.title).toEqual("Concrete Skills")
  })

  it("setResults should set results", () => {
    // Arrange
    const paginatedSkills = createMockPaginatedSkills()

    // Act
    component.setResults(paginatedSkills)

    // Assert
    expect(component.results).toEqual(paginatedSkills)
    expect(component.selectedSkills).toBeFalsy()
    expect(component.totalCount).toEqual(paginatedSkills.totalCount)
    expect(component.curPageCount).toEqual(paginatedSkills.skills.length)
    expect(component.getSelectAllCount()).toEqual(component.curPageCount)
  })

  it("skillCountLabel should be correct", () => {
    component.setResults(createMockPaginatedSkills(0, 0))
    expect(component.skillCountLabel).toEqual("0 RSDs")

    component.setResults(createMockPaginatedSkills(0, 1))
    expect(component.skillCountLabel).toEqual("1 RSD")

    component.setResults(createMockPaginatedSkills(1, 1))
    expect(component.skillCountLabel).toEqual("1 RSD")

    component.setResults(createMockPaginatedSkills(1, 10))
    expect(component.skillCountLabel).toEqual("10 RSDs")
  })

  it("totalCount should be correct", () => {
    component.setResults(createMockPaginatedSkills(11, 23))
    expect(component.totalCount).toEqual(23)
  })

  it("curPageCount should be correct", () => {
    component.setResults(createMockPaginatedSkills(11, 23))
    expect(component.curPageCount).toEqual(11)
  })

  it("getMobileSortOptions should be correct", () => {
    const result = component.getMobileSortOptions()
    expect(result).toEqual({
      "name.asc": "Category (ascending)",
      "name.desc": "Category (descending)",
      "skill.asc": "RSD Name (ascending)",
      "skill.desc": "RSD Name (descending)",
    })
  })

  it("emptyResults should be correct", () => {
    component.setResults(createMockPaginatedSkills(0, 0))
    expect(component.emptyResults).toBeTruthy()

    component.setResults(createMockPaginatedSkills(1, 1))
    expect(component.emptyResults).toBeFalsy()
  })

  it("firstRecordNo should return", () => {
    // Note that we're choosing primes for inputs to avoid math irregularities
    component.from = 11
    expect(component.firstRecordNo).toEqual(12)
  })

  it("lastRecordNo should return", () => {
    component.from = 11
    component.size = 47
    component.setResults(createMockPaginatedSkills(29, 123))
    expect(component.lastRecordNo).toEqual(11 + 29)  // Expecting from+curPageCount

    component.from = 11
    component.size = 47
    component.setResults(createMockPaginatedSkills(12, 13))
    expect(component.lastRecordNo).toEqual(13)  // Expecting totalCount
  })

  it("totalPageCount should return", () => {
    component.size = 47
    component.setResults(createMockPaginatedSkills(29, 123))
    expect(component.totalPageCount).toEqual(Math.trunc((123 + 46) / 47))
  })

  it("currentPageNo should return", () => {
    component.from = 11
    component.size = 47
    expect(component.currentPageNo).toEqual(Math.trunc(11 / 47) + 1)
  })

  it("navigateToPage should load next page", () => {
    spyOn(component, "loadNextPage").and.callThrough()

    component.from = 11
    component.size = 47
    component.navigateToPage(31)
    expect(component.from).toEqual((31 - 1) * 47)
    expect(component.loadNextPage).toHaveBeenCalledTimes(1)
  })

  it("actionsVisible should be true", () => {
    expect(component.actionsVisible()).toBeTruthy()
  })

  it("publishVisible should be correct", () => {
    /* Assumption: status doesn't matter! */
    // id, date undefined
    const result0 = component.publishVisible(
      createMockSkillSummary("id1", PublishStatus.Draft, "")
    )
    expect(result0).toBeTruthy()

    // No skills
    component.selectedSkills = []
    const result1 = component.publishVisible(undefined)
    expect(result1).toBeFalsy()

    // one with date, one without
    component.selectedSkills = [
      createMockSkillSummary("id1", PublishStatus.Draft, ""),  // No publishDate
      createMockSkillSummary("id1", PublishStatus.Draft)  // Has publishDate
    ]
    const result2 = component.publishVisible(undefined)
    expect(result2).toBeTruthy()
  })

  it("archiveVisible should be correct", () => {
    /* Assumption: status *does* matter. */
    // id, different status
    const result0 = component.archiveVisible(
      createMockSkillSummary("id1", PublishStatus.Draft)
    )
    expect(result0).toBeTruthy()

    // No skills
    component.selectedSkills = []
    const result1 = component.archiveVisible(undefined)
    expect(result1).toBeFalsy()

    // one with date, one without
    component.selectedSkills = [
      createMockSkillSummary("id1", PublishStatus.Draft),
      createMockSkillSummary("id1", PublishStatus.Archived)
    ]
    const result2 = component.archiveVisible(undefined)
    expect(result2).toBeTruthy()
  })

  it("unarchiveVisible should be correct", () => {
    /* Assumption: status *does* matter. */
    // id, different status
    const result0 = component.unarchiveVisible(
      createMockSkillSummary("id1", PublishStatus.Archived)
    )
    expect(result0).toBeTruthy()

    // No skills
    component.selectedSkills = []
    const result1 = component.unarchiveVisible(undefined)
    expect(result1).toBeFalsy()

    // one with date, one without
    component.selectedSkills = [
      createMockSkillSummary("id1", PublishStatus.Draft),
      createMockSkillSummary("id1", PublishStatus.Archived)
    ]
    const result2 = component.unarchiveVisible(undefined)
    expect(result2).toBeTruthy()
  })

  it("addToCollectionVisible should be correct", () => {    // No skills
    /* Assumption: skill parameter does not matter. */
    component.selectedSkills = []
    const result1 = component.addToCollectionVisible(undefined)
    expect(result1).toBeFalsy()

    // one with date, one without
    component.selectedSkills = [
      createMockSkillSummary("id1", PublishStatus.Draft),
      createMockSkillSummary("id1", PublishStatus.Archived)
    ]
    const result2 = component.addToCollectionVisible(undefined)
    expect(result2).toBeTruthy()
  })

  it("handleFiltersChanged should be correct", () => {
    // Arrange
    const newFilters = new Set<PublishStatus>([PublishStatus.Unarchived])
    spyOn(component, "loadNextPage").and.callThrough()

    // Act
    component.handleFiltersChanged(newFilters)

    // Assert
    expect(component.selectedFilters).toEqual(newFilters)
    expect(component.loadNextPage).toHaveBeenCalledTimes(1)
  })

  it("handlePageClicked should be correct", () => {
    spyOn(component, "loadNextPage").and.callThrough()

    component.from = 11
    component.size = 47
    component.handlePageClicked(31)
    expect(component.from).toEqual((31 - 1) * 47)
    expect(component.loadNextPage).toHaveBeenCalledTimes(1)
  })

  it("handleNewSelection should be correct", () => {
    const selected = createMockPaginatedSkills(2, 3).skills
    component.handleNewSelection(selected)
    expect(component.selectedSkills).toEqual(selected)
  })

  it("handleHeaderColumnSort should load next page", () => {
    // Arrange
    spyOn(component, "loadNextPage").and.callThrough()
    const sort = ApiSortOrder.SkillDesc
    component.columnSort = ApiSortOrder.NameAsc
    component.from = 47

    // Act
    component.handleHeaderColumnSort(sort)

    // Assert
    expect(component.columnSort).toEqual(sort)
    expect(component.from).toEqual(0)
    expect(component.loadNextPage).toHaveBeenCalledTimes(1)
  })

  it("rowActions should be correct", () => {
    let rowActions = component.rowActions()
    expect(rowActions).toBeTruthy()

    const skill0 = createMockSkillSummary("id0", PublishStatus.Draft)
    const action0 = rowActions[0]
    expect(action0.label).toEqual("Archive RSD")
    expect(action0 && action0.callback).toBeTruthy()
    expect(action0.callback?.(action0, skill0)).toBeFalsy()  // Always false
    expect(action0.visible?.(skill0)).toBeTruthy()  // != Archived

    const skill1 = createMockSkillSummary("id1", PublishStatus.Archived)
    const action1 = rowActions[1]
    expect(action1.label).toEqual("Unarchive RSD")
    expect(action1 && action1.callback).toBeTruthy()
    expect(action1.callback?.(action1, skill1)).toBeFalsy()  // Always false
    expect(action1.visible?.(skill1)).toBeTruthy()  // == Archived

    spyOn(window, "confirm").and.returnValue(true)
    const skill2 = createMockSkillSummary("id2", PublishStatus.Draft, "")
    const action2 = rowActions[2]
    expect(action2.label).toEqual("Publish RSD")
    expect(action2 && action2.callback).toBeTruthy()
    expect(action2.callback?.(action2, skill2)).toBeFalsy()  // Always false
    expect(action2.visible?.(skill2)).toBeTruthy()  // !has publish date

    component.showAddToCollection = true
    rowActions = component.rowActions()
    let skill3 = createMockSkillSummary("id3", PublishStatus.Draft, "")
    let action3 = rowActions[3]
    expect(action3.label).toEqual("Add to Collection")
    expect(action3 && action3.callback).toBeTruthy()
    expect(action3.callback?.(action3, skill3)).toBeFalsy()  // Always false

    component.showAddToCollection = false
    rowActions = component.rowActions()
    skill3 = createMockSkillSummary("id3", PublishStatus.Draft, "")
    action3 = rowActions[3]
    expect(action3.label).toEqual("Remove from Collection")
    expect(action3 && action3.callback).toBeTruthy()
    expect(action3.callback?.(action3, skill3)).toBeFalsy()  // Always false
  })

  it("tableActions should be correct", () => {
    let tableActions = component.tableActions()
    expect(tableActions).toBeTruthy()

    const skill0 = createMockSkillSummary("id0", PublishStatus.Draft, "")
    const action0 = tableActions[0]
    expect(action0.label).toEqual("Back to Top")
    expect(action0 && action0.callback).toBeTruthy()
    expect(action0.callback?.(action0, skill0)).toBeFalsy()  // Always false
    expect(action0.visible?.(skill0)).toBeTruthy()  // Always true

    spyOn(window, "confirm").and.returnValue(true)
    const skill1 = createMockSkillSummary("id1", PublishStatus.Draft, "")
    const action1 = tableActions[1]
    expect(action1.label).toEqual("Publish")
    expect(action1 && action1.callback).toBeTruthy()
    expect(action1.callback?.(action1, skill1)).toBeFalsy()  // Always false
    expect(action1.visible?.(skill1)).toBeTruthy()  // !has publish date

    const skill2 = createMockSkillSummary("id2", PublishStatus.Draft)
    const action2 = tableActions[2]
    expect(action2.label).toEqual("Archive")
    expect(action2 && action2.callback).toBeTruthy()
    expect(action2.callback?.(action2, skill2)).toBeFalsy()  // Always false
    expect(action2.visible?.(skill2)).toBeTruthy()  // == Archived

    const skill3 = createMockSkillSummary("id3", PublishStatus.Draft)
    const action3 = tableActions[3]
    expect(action3.label).toEqual("Unarchive")
    expect(action3 && action3.callback).toBeTruthy()
    expect(action3.callback?.(action3, skill3)).toBeFalsy()  // Always false
    expect(action3.visible?.(skill3)).toBeFalsy()  // != Archived

    component.selectedSkills = [
      createMockSkillSummary("id1", PublishStatus.Draft),
    ]
    component.showAddToCollection = true
    tableActions = component.tableActions()
    let skill4 = createMockSkillSummary("id4", PublishStatus.Archived)
    let action4 = tableActions[4]
    expect(action4.label).toEqual("Add to Collection")
    expect(action4 && action4.callback).toBeTruthy()
    expect(action4.callback?.(action4, skill4)).toBeFalsy()  // Always false
    expect(action4.visible?.(skill4)).toBeTruthy()  // There are selected skills

    component.selectedSkills = [
      createMockSkillSummary("id1", PublishStatus.Draft),
    ]
    component.showAddToCollection = false
    tableActions = component.tableActions()
    skill4 = createMockSkillSummary("id4", PublishStatus.Archived)
    action4 = tableActions[4]
    expect(action4.label).toEqual("Remove from Collection")
    expect(action4 && action4.callback).toBeTruthy()
    expect(action4.callback?.(action4, skill4)).toBeFalsy()  // Always false
    expect(action4.visible?.(skill4)).toBeTruthy()  // There are selected skills
  })

  it("getSelectedSkills should be correct", () => {
    component.selectedSkills = [
      createMockSkillSummary("id1", PublishStatus.Draft),
    ]

    expect(component.getSelectedSkills(
      undefined
    )).toEqual(component.selectedSkills)

    const skill = createMockSkillSummary()
    expect(component.getSelectedSkills(
    )).toEqual([skill])
  })

  it("selectedUuids should be correct", () => {
    component.selectedSkills = [
      createMockSkillSummary("id1", PublishStatus.Draft),
    ]

    expect(component.selectedUuids(
      undefined
    )).toEqual([component.selectedSkills[0].uuid])

    const skill = createMockSkillSummary()
    expect(component.selectedUuids(
    )).toEqual([skill.uuid])
  })

  it("getApiSearch should be correct", () => {
    const skill = createMockSkillSummary("id2", PublishStatus.Draft)
    const uuids = [skill.uuid]

    expect(component.getApiSearch(skill)).toEqual(
      new ApiSearch({ uuids })
    )

    component.selectedSkills = undefined
    expect(component.getApiSearch(undefined)).toEqual(
      undefined
    )

    component.selectedSkills = [
      createMockSkillSummary("id1", PublishStatus.Draft)
    ]
    expect(component.getApiSearch(undefined)).toEqual(
      new ApiSearch({ uuids: [component.selectedSkills[0].uuid] })
    )
  })

  it("submitStatusChange should be correct", () => {
    component.selectedSkills = undefined
    expect(component.submitStatusChange(PublishStatus.Published, "published", undefined)).toEqual(
      false
    )

    const skill = createMockSkillSummary("id2", PublishStatus.Draft)
    expect(component.submitStatusChange(PublishStatus.Published, "published", skill)).toEqual(
      false
    )
  })

  it("getSelectAllEnabled should be true", () => {
    expect(component.getSelectAllEnabled()).toBeTruthy()
  })
})
