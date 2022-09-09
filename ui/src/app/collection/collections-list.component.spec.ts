// noinspection MagicNumberJS,LocalVariableNamingConventionJS

import { Component, Type } from "@angular/core"
import { async, ComponentFixture, TestBed } from "@angular/core/testing"
import { RouterTestingModule } from "@angular/router/testing"
import { createMockCollectionSummary, createMockPaginatedCollections } from "../../../test/resource/mock-data"
import {AuthServiceStub, CollectionServiceStub} from "../../../test/resource/mock-stubs"
import { PublishStatus } from "../PublishStatus"
import { ApiSortOrder } from "../richskill/ApiSkill"
import { ApiSearch, PaginatedCollections } from "../richskill/service/rich-skill-search.service"
import { ToastService } from "../toast/toast.service"
import { CollectionsListComponent } from "./collections-list.component"
import { CollectionService } from "./service/collection.service"
import {AuthService} from "../auth/auth-service";


@Component({
  selector: "app-concrete-component",
  template: ``
})
class ConcreteComponent extends CollectionsListComponent {
  matchingQuery?: string[]
  title = "Concrete Collections"

  loadNextPage(): void {}

  handleSelectAll(selectAllChecked: boolean): void {}

  public setResults(results: PaginatedCollections): void {
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


describe("CollectionsListComponent", () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        ConcreteComponent
      ],
      imports: [
        RouterTestingModule  // CollectionsListComponent depends on the Router
      ],
      providers: [
        ToastService,
        { provide: CollectionService, useClass: CollectionServiceStub },
        { provide: AuthService, useClass: AuthServiceStub },
      ]
    })

    createComponent(ConcreteComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("title should be correct", () => {
    expect(component.title).toEqual("Concrete Collections")
  })

  it("setResults should set results", () => {
    // Arrange
    const paginatedCollections = createMockPaginatedCollections()

    // Act
    component.setResults(paginatedCollections)

    // Assert
    expect(component.results).toEqual(paginatedCollections)
    expect(component.selectedCollections).toBeFalsy()
    expect(component.totalCount).toEqual(paginatedCollections.totalCount)
    expect(component.curPageCount).toEqual(paginatedCollections.collections.length)
    expect(component.getSelectAllCount()).toEqual(component.curPageCount)
  })

  it("collectionCountLabel should be correct", () => {
    component.setResults(createMockPaginatedCollections(0, 0))
    expect(component.collectionCountLabel).toEqual("0 collections")

    component.setResults(createMockPaginatedCollections(0, 1))
    expect(component.collectionCountLabel).toEqual("1 collection")

    component.setResults(createMockPaginatedCollections(1, 1))
    expect(component.collectionCountLabel).toEqual("1 collection")

    component.setResults(createMockPaginatedCollections(1, 10))
    expect(component.collectionCountLabel).toEqual("10 collections")
  })

  it("totalCount should be correct", () => {
    component.setResults(createMockPaginatedCollections(11, 23))
    expect(component.totalCount).toEqual(23)
  })

  it("curPageCount should be correct", () => {
    component.setResults(createMockPaginatedCollections(11, 23))
    expect(component.curPageCount).toEqual(11)
  })

  it("getMobileSortOptions should be correct", () => {
    const result = component.getMobileSortOptions()
    expect(result).toEqual({
      "name.asc": "Collection name (ascending)",
      "name.desc": "Collection name (descending)",
      "skill.asc": "Skill count (ascending)",
      "skill.desc": "Skill count (descending)",
    })
  })

  it("emptyResults should be correct", () => {
    component.setResults(createMockPaginatedCollections(0, 0))
    expect(component.emptyResults).toBeTruthy()

    component.setResults(createMockPaginatedCollections(1, 1))
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
    component.setResults(createMockPaginatedCollections(29, 123))
    expect(component.lastRecordNo).toEqual(11 + 29)  // Expecting from+curPageCount

    component.from = 11
    component.size = 47
    component.setResults(createMockPaginatedCollections(12, 13))
    expect(component.lastRecordNo).toEqual(13)  // Expecting totalCount
  })

  it("totalPageCount should return", () => {
    component.size = 47
    component.setResults(createMockPaginatedCollections(29, 123))
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
      createMockCollectionSummary("id1", PublishStatus.Draft, "")
    )
    expect(result0).toBeTruthy()

    // No collections
    component.selectedCollections = []
    const result1 = component.publishVisible(undefined)
    expect(result1).toBeFalsy()

    // one with date, one without
    component.selectedCollections = [
      createMockCollectionSummary("id1", PublishStatus.Draft, ""),  // No publishDate
      createMockCollectionSummary("id1", PublishStatus.Draft)  // Has publishDate
    ]
    const result2 = component.publishVisible(undefined)
    expect(result2).toBeTruthy()
  })

  it("archiveVisible should be correct", () => {
    /* Assumption: status *does* matter. */
    // id, different status
    const result0 = component.archiveVisible(
      createMockCollectionSummary("id1", PublishStatus.Draft)
    )
    expect(result0).toBeTruthy()

    // No collections
    component.selectedCollections = []
    const result1 = component.archiveVisible(undefined)
    expect(result1).toBeFalsy()

    // one with date, one without
    component.selectedCollections = [
      createMockCollectionSummary("id1", PublishStatus.Draft),
      createMockCollectionSummary("id1", PublishStatus.Archived)
    ]
    const result2 = component.archiveVisible(undefined)
    expect(result2).toBeTruthy()
  })

  it("unarchiveVisible should be correct", () => {
    /* Assumption: status *does* matter. */
    // id, different status
    const result0 = component.unarchiveVisible(
      createMockCollectionSummary("id1", PublishStatus.Archived)
    )
    expect(result0).toBeTruthy()

    // No collections
    component.selectedCollections = []
    const result1 = component.unarchiveVisible(undefined)
    expect(result1).toBeFalsy()

    // one with date, one without
    component.selectedCollections = [
      createMockCollectionSummary("id1", PublishStatus.Draft),
      createMockCollectionSummary("id1", PublishStatus.Archived)
    ]
    const result2 = component.unarchiveVisible(undefined)
    expect(result2).toBeTruthy()
  })

  it("handleFiltersChanged should be correct", () => {
    // Arrange
    const newFilters = new Set<PublishStatus>([PublishStatus.Unarchived])
    spyOn(component, "loadNextPage").and.callThrough()
    let called = false
    component.clearSelectedItemsFromTable.subscribe(() => {
      called = true
    })

    // Act
    component.handleFiltersChanged(newFilters)
    while (!called) {}

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
    const selected = createMockPaginatedCollections(2, 3).collections
    component.handleNewSelection(selected)
    expect(component.selectedCollections).toEqual(selected)
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
    const rowActions = component.rowActions()
    expect(rowActions).toBeTruthy()

    const collection0 = createMockCollectionSummary("id1", PublishStatus.Draft)
    const action0 = rowActions[0]
    expect(action0.label).toEqual("Archive collection")
    expect(action0 && action0.callback).toBeTruthy()
    expect(action0.callback?.(action0, collection0)).toBeFalsy()  // Always false
    expect(action0.visible?.(collection0)).toBeTruthy()  // != Archived

    const collection1 = createMockCollectionSummary("id2", PublishStatus.Archived)
    const action1 = rowActions[1]
    expect(action1.label).toEqual("Unarchive collection")
    expect(action1 && action1.callback).toBeTruthy()
    expect(action1.callback?.(action1, collection1)).toBeFalsy()  // Always false
    expect(action1.visible?.(collection1)).toBeTruthy()  // == Archived

    spyOn(window, "confirm").and.returnValue(true)
    const collection2 = createMockCollectionSummary("id3", PublishStatus.Draft, "")
    const action2 = rowActions[2]
    expect(action2.label).toEqual("Publish collection")
    expect(action2 && action2.callback).toBeTruthy()
    expect(action2.callback?.(action2, collection2)).toBeFalsy()  // Always false
    expect(action2.visible?.(collection2)).toBeTruthy()  // !has publish date
  })

  it("tableActions should be correct", () => {
    const tableActions = component.tableActions()
    expect(tableActions).toBeTruthy()

    const collection0 = createMockCollectionSummary("id3", PublishStatus.Draft, "")
    const action0 = tableActions[0]
    expect(action0.label).toEqual("Back to Top")
    expect(action0 && action0.callback).toBeTruthy()
    expect(action0.callback?.(action0, collection0)).toBeFalsy()  // Always false
    expect(action0.visible?.(collection0)).toBeTruthy()  // Always true

    spyOn(window, "confirm").and.returnValue(true)
    const collection1 = createMockCollectionSummary("id3", PublishStatus.Draft, "")
    const action1 = tableActions[1]
    expect(action1.label).toEqual("Publish")
    expect(action1 && action1.callback).toBeTruthy()
    expect(action1.callback?.(action1, collection1)).toBeFalsy()  // Always false
    expect(action1.visible?.(collection1)).toBeTruthy()  // !has publish date

    const collection2 = createMockCollectionSummary("id1", PublishStatus.Draft)
    const action2 = tableActions[2]
    expect(action2.label).toEqual("Archive")
    expect(action2 && action2.callback).toBeTruthy()
    expect(action2.callback?.(action2, collection2)).toBeFalsy()  // Always false
    expect(action2.visible?.(collection2)).toBeTruthy()  // == Archived

    const collection3 = createMockCollectionSummary("id2", PublishStatus.Archived)
    const action3 = tableActions[3]
    expect(action3.label).toEqual("Unarchive")
    expect(action3 && action3.callback).toBeTruthy()
    expect(action3.callback?.(action3, collection3)).toBeFalsy()  // Always false
    expect(action3.visible?.(collection3)).toBeTruthy()  // != Archived
  })

  it("getSelectedSkills should be correct", () => {
    component.selectedCollections = [
      createMockCollectionSummary("id1", PublishStatus.Draft),
    ]

    expect(component.getSelectedSkills(
      undefined
    )).toEqual(component.selectedCollections)

    const collection = createMockCollectionSummary()
    expect(component.getSelectedSkills(
    )).toEqual([collection])
  })

  it("selectedUuids should be correct", () => {
    component.selectedCollections = [
      createMockCollectionSummary("id1", PublishStatus.Draft),
    ]

    expect(component.selectedUuids(
      undefined
    )).toEqual([component.selectedCollections[0].uuid])

    const collection = createMockCollectionSummary()
    expect(component.selectedUuids(
    )).toEqual([collection.uuid])
  })

  it("getApiSearch should be correct", () => {
    const collection = createMockCollectionSummary("id2", PublishStatus.Draft)
    const uuids = [collection.uuid]

    expect(component.getApiSearch(collection)).toEqual(
      new ApiSearch({ uuids })
    )

    component.selectedCollections = undefined
    expect(component.getApiSearch(undefined)).toEqual(
      undefined
    )

    component.selectedCollections = [
      createMockCollectionSummary("id1", PublishStatus.Draft)
    ]
    expect(component.getApiSearch(undefined)).toEqual(
      new ApiSearch({ uuids: [component.selectedCollections[0].uuid] })
    )
  })

  it("submitStatusChange should be correct", () => {
    component.selectedCollections = undefined
    expect(component.submitStatusChange(PublishStatus.Published, "published", undefined)).toEqual(
      false
    )

    const collection = createMockCollectionSummary("id2", PublishStatus.Draft)
    expect(component.submitStatusChange(PublishStatus.Published, "published", collection)).toEqual(
      false
    )
  })

  it("getSelectAllEnabled should be true", () => {
    expect(component.getSelectAllEnabled()).toBeTruthy()
  })
})
