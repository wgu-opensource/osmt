import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {ApiSortOrder} from "../richskill/ApiSkill"
import {TableActionDefinition} from "./skills-library-table/has-action-definitions"
import {SvgHelper, SvgIcon} from "../core/SvgHelper"
import {Observable} from "rxjs"

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
  @Input() selectAllCount?: number
  @Input() selectAllEnabled = true
  @Input() clearSelected = new Observable<void>()

  @Output() columnSorted = new EventEmitter<ApiSortOrder>()

  @Output() rowSelected: EventEmitter<SummaryT[]> = new EventEmitter<SummaryT[]>()
  @Output() selectAllSelected = new EventEmitter<boolean>()
  @Output() focusActionBar = new EventEmitter<void>()

  selectedItems: Set<SummaryT> = new Set()

  checkIcon = SvgHelper.path(SvgIcon.CHECK)

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
    } else {
      this.selectedItems.add(item)
    }
    this.rowSelected.emit(Array.from(this.selectedItems))
  }

  handleSelectAll(event: Event): void {
    const checkbox = event.target as HTMLInputElement
    const selected: boolean = checkbox.checked
    this.selectAllSelected.emit(selected)
    if (selected) {
      this.items.forEach(it => this.selectedItems.add(it))
    } else {
      this.selectedItems.clear()
    }
    this.rowSelected.emit(Array.from(this.selectedItems))
  }
}
