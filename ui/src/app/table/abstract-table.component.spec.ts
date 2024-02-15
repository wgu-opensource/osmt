import {Injectable, Type} from "@angular/core"
import {async, ComponentFixture, TestBed} from "@angular/core/testing"
import {createMockSkillSummary} from "../../../test/resource/mock-data"
import {ApiSortOrder} from "../richskill/ApiSkill"
import {ApiSkillSummary, ISkillSummary} from "../richskill/ApiSkillSummary"
import {AbstractTableComponent} from "./abstract-table.component"
import {SelectAllEvent} from "../models"
import {SelectAll} from "./select-all/select-all.component"


@Injectable({
  providedIn: "root"
})
export class ConcreteComponent extends AbstractTableComponent<ApiSkillSummary> {
  constructor() {
    super()
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


describe("AbstractTableComponent", () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        ConcreteComponent
      ],
    })
    .compileComponents()

    createComponent(ConcreteComponent)
  }))

  it("should be created", () => {
    expect(component).toBeTruthy()
  })

  it("getNameSort should detect sort order", () => {
    [
      { input: ApiSortOrder.NameAsc, expected: true },
      { input: ApiSortOrder.NameDesc, expected: false },
      { input: ApiSortOrder.SkillAsc, expected: undefined },
      { input: ApiSortOrder.SkillDesc, expected: undefined }
    ].forEach((param) => {
      // Arrange
      component.currentSort = param.input

      // Act/Assert
      expect(component.getNameSort()).toEqual(param.expected)
    })
  })

  it("getSkillSort should detect sort order", () => {
    [
      { input: ApiSortOrder.NameAsc, expected: undefined },
      { input: ApiSortOrder.NameDesc, expected: undefined },
      { input: ApiSortOrder.SkillAsc, expected: true },
      { input: ApiSortOrder.SkillDesc, expected: false }
    ].forEach((param) => {
      // Arrange
      component.currentSort = param.input

      // Act/Assert
      expect(component.getSkillSort()).toEqual(param.expected)
    })
  })

  it("sortColumn should set sort order", () => {
    [
      { col: "name", ascending: true, expected: ApiSortOrder.NameAsc },
      { col: "name", ascending: false, expected: ApiSortOrder.NameDesc },
      { col: "skill", ascending: true, expected: ApiSortOrder.SkillAsc },
      { col: "skill", ascending: false, expected: ApiSortOrder.SkillDesc }
    ].forEach((param) => {
      // Arrange/Act
      component.sortColumn(param.col, param.ascending)

      // Assert
      expect(component.currentSort).toEqual(param.expected)
    })
  })

  it("mobileSortColumn should set sort order", () => {
    [
      { expected: ApiSortOrder.NameAsc },
      { expected: ApiSortOrder.NameDesc },
      { expected: ApiSortOrder.SkillAsc },
      { expected: ApiSortOrder.SkillDesc }
    ].forEach((param) => {
      // Arrange
      component.mobileSortColumn(param.expected)

      // Act/Assert
      expect(component.currentSort).toEqual(param.expected)
    })
  })

  it("numberOfSelected should be correct", () => {
    // Arrange
    component.selectedItems.clear()
    component.selectedItems.add(createMockSkillSummary())

    // Act
    const result = component.numberOfSelected()

    // Assert
    expect(result).toEqual(1)
  })

  it("isSelected should detect item", () => {
    // Arrange
    const item = createMockSkillSummary()
    component.selectedItems.clear()
    component.selectedItems.add(item)

    // Act
    const result = component.isSelected(item)

    // Assert
    expect(result).toBeTrue()
  })

  it("getSelectAllCount should be correct", () => {
    // Arrange
    const item = createMockSkillSummary()
    component.items = [ item ]
    component.selectAllCount = undefined

    // Act
    const result = component.getSelectAllCount()

    // Assert
    expect(result).toEqual(1)
  })

  it("getSelectAllCount should be overridden", () => {
    // Arrange
    const item = createMockSkillSummary()
    component.items = [ item ]
    component.selectAllCount = 2

    // Act
    const result = component.getSelectAllCount()

    // Assert
    expect(result).toEqual(2)
  })

  it("onRowToggle should add", () => {
    // Arrange
    const item = createMockSkillSummary()
    component.selectedItems.clear()
    spyOn(component.rowSelected, "emit")

    // Act
    component.onRowToggle(item)

    // Assert
    expect(component.selectedItems.size).toEqual(1)
    expect(component.rowSelected.emit).toHaveBeenCalledWith([item])
  })
  it("onRowToggle should remove", () => {
    // Arrange
    const item = createMockSkillSummary()
    component.selectedItems.clear()
    component.selectedItems.add(item)
    spyOn(component.rowSelected, "emit")

    // Act
    component.onRowToggle(item)

    // Assert
    expect(component.selectedItems.size).toEqual(0)
    expect(component.rowSelected.emit).toHaveBeenCalledWith([])
  })

  it("handleSelect should emit false", () => {
    const evt: SelectAllEvent = {
      selected: true,
      value: SelectAll.SELECT_PAGE
    }
    const spyEmit = spyOn(component.selectAllSelected, "emit")
    component.handleSelectAll(evt)
    expect(spyEmit).toHaveBeenCalledWith(false)
  })

  it("handleSelectAll should add items", () => {
    // Arrange
    const evt: SelectAllEvent = {
      selected: true,
      value: SelectAll.SELECT_ALL
    }
    const item = createMockSkillSummary()
    component.selectedItems.clear()
    component.items = [item]
    const expected: ISkillSummary[] = [item]
    spyOn(component.selectAllSelected, "emit")
    spyOn(component.rowSelected, "emit")

    // Act
    component.handleSelectAll(evt)

    // Assert
    expect(component.selectedItems).toEqual(new Set(expected))
    expect(component.selectAllSelected.emit).toHaveBeenCalledWith(true)
    expect(component.rowSelected.emit).toHaveBeenCalledWith(expected)
  })

  it("handleSelectAll should remove items", () => {
    // Arrange
    const evt: SelectAllEvent = {
      selected: false,
      value: SelectAll.SELECT_ALL
    }
    const item = createMockSkillSummary()
    component.selectedItems.clear()
    component.selectedItems.add(item)
    const expected: ISkillSummary[] = []
    spyOn(component.selectAllSelected, "emit")
    spyOn(component.rowSelected, "emit")

    // Act
    component.handleSelectAll(evt)

    // Assert
    expect(component.selectedItems).toEqual(new Set(expected))
    expect(component.selectAllSelected.emit).toHaveBeenCalledWith(false)
    expect(component.rowSelected.emit).toHaveBeenCalledWith(expected)
  })

  it("shift is pressed has the correct value", () => {
    expect(component.isShiftPressed).toBeFalse()
    component.onKeydownHandler()
    expect(component.isShiftPressed).toBeTrue()
    component.onKeyupHandler()
    expect(component.isShiftPressed).toBeFalse()
  })

  it("on row toggle should call shift selection", () => {
    const apiSkillSummary = createMockSkillSummary()
    const spyShiftSelection = spyOn(component, "shiftSelection")
    component.onRowToggle(apiSkillSummary)
    expect(spyShiftSelection).toHaveBeenCalledWith(apiSkillSummary)
  })
})
