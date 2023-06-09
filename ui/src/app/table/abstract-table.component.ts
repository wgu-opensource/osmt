import {Component, EventEmitter, HostListener, Input, OnInit, Output} from "@angular/core"
import {ApiSortOrder} from "../richskill/ApiSkill"
import {TableActionDefinition} from "./skills-library-table/has-action-definitions"
import {SvgHelper, SvgIcon} from "../core/SvgHelper"
import {Observable} from "rxjs"
import {SelectAll} from "./select-all/select-all.component"
import {SelectAllEvent} from "../models"

/**
 * Implement row components to hold datasets and figure out how to dynamically pass and use them
 */
@Component({
  selector: "app-abstract-table",
  template: ``
})
export class AbstractTableComponent<SummaryT> implements OnInit {

  @Input() items: SummaryT[] = []
  @Input() currentSort?: ApiSortOrder = undefined
  @Input() rowActions: TableActionDefinition[] = []
  @Input() showRowActions: boolean = true
  @Input() selectAllCount?: number
  @Input() selectAllEnabled = true
  @Input() clearSelected = new Observable<void>()

  @Input() mobileSortOptions: {[s: string]: string} = {}

  @Output() columnSorted = new EventEmitter<ApiSortOrder>()

  @Output() rowSelected: EventEmitter<SummaryT[]> = new EventEmitter<SummaryT[]>()
  @Output() selectAllSelected = new EventEmitter<boolean>()
  @Output() focusActionBar = new EventEmitter<void>()

  selectedItems: Set<SummaryT> = new Set()

  checkIcon = SvgHelper.path(SvgIcon.CHECK)
  isShiftPressed = false
  isAllSelected = false

  constructor() { }

  ngOnInit(): void {
    this.clearSelected.subscribe(next => this.selectedItems = new Set())
  }

  getNameSort(): boolean | undefined {
    if (this.currentSort) {
      switch (this.currentSort) {
        case ApiSortOrder.NameAsc: return true
        case ApiSortOrder.NameDesc: return false
      }
    }
    return undefined
  }

  getSkillSort(): boolean | undefined {
    if (this.currentSort) {
      switch (this.currentSort) {
        case ApiSortOrder.SkillAsc: return true
        case ApiSortOrder.SkillDesc: return false
      }
    }
    return undefined
  }

  sortColumn(column: string, ascending: boolean): void {
    if (column.toLowerCase() === "name") {
      if (ascending) {
        this.currentSort = ApiSortOrder.NameAsc
      } else {
        this.currentSort = ApiSortOrder.NameDesc
      }
    } else if (column.toLowerCase() === "skill") {
      if (ascending) {
        this.currentSort = ApiSortOrder.SkillAsc
      } else {
        this.currentSort = ApiSortOrder.SkillDesc
      }
    }
    this.columnSorted.emit(this.currentSort)
  }

  mobileSortColumn(newSort: ApiSortOrder): void {
    this.currentSort = newSort
    this.columnSorted.emit(this.currentSort)
  }

  isSelected(item: SummaryT): boolean {
    return this.selectedItems.has(item)
  }

  numberOfSelected(): number {
    return this.selectedItems.size
  }

  getSelectAllCount(): number {
    return (this.selectAllCount !== undefined) ? this.selectAllCount : this.items.length
  }

  // Every time a row is toggled, emit the current list of all selected rows
  onRowToggle(item: SummaryT): void {
    if (this.selectedItems.has(item)) {
      this.selectedItems.delete(item)
      if (this.isAllSelected) {
        this.isAllSelected = false
      }
    } else {
      this.selectedItems.add(item)
      this.shiftSelection(item)
    }
    this.selectAllSelected.emit(this.isAllSelected)
    this.rowSelected.emit(Array.from(this.selectedItems))
  }

  shiftSelection(item: SummaryT): void {}

  handleSelectAll(event: SelectAllEvent): void {
    this.isAllSelected = event.selected
    const isAllResultsSelected: boolean = event.selected && event.value === SelectAll.SELECT_ALL // select all across pages
    this.selectAllSelected.emit(isAllResultsSelected)
    if (this.isAllSelected) {
      this.items.forEach(it => this.selectedItems.add(it))
    } else {
      this.selectedItems.clear()
    }
    this.rowSelected.emit(Array.from(this.selectedItems))
  }

  @HostListener("document:keydown.shift", ["$event"])
  onKeydownHandler(): void {
    this.isShiftPressed = true
  }

  @HostListener("document:keyup.shift", ["$event"])
  onKeyupHandler(): void {
    this.isShiftPressed = false
  }

}
