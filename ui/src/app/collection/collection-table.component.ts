import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {ApiCollectionSummary, IApiSkillSummary, ICollectionSummary} from "../richskill/ApiSkillSummary";
import {TableActionDefinition} from "../table/skills-library-table/has-action-definitions";
import {SvgHelper, SvgIcon} from "../core/SvgHelper";
import {ApiSortOrder} from "../richskill/ApiSkill";
import {ICollection} from "./ApiCollection";

@Component({
  selector: "app-collection-table",
  templateUrl: "./collection-table.component.html"
})
export class CollectionTableComponent implements OnInit {

  @Input() collections: ApiCollectionSummary[] = []
  @Input() rowActions: TableActionDefinition[] = []

  @Output() rowSelected: EventEmitter<ICollectionSummary[]> = new EventEmitter<ICollectionSummary[]>()
  @Output() selectAllSelected = new EventEmitter<boolean>()
  @Input() selectAllCount?: number
  @Input() selectAllEnabled: boolean = true

  @Input() allowSorting = false
  @Input() currentSort: ApiSortOrder | undefined = undefined
  @Output() columnSorted = new EventEmitter<ApiSortOrder>()

  checkIcon = SvgHelper.path(SvgIcon.CHECK)

  constructor() {
  }

  ngOnInit(): void {
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

  getSelectedCollections(): ICollectionSummary[] {
    return []
    // return this.preparedSkills.filter(row => row.selected).map(row => row.skill)
  }

  getSelectAllCount(): number {
    return -1
    // return (this.selectAllCount !== undefined) ? this.selectAllCount : this.preparedSkills.length
  }

  handleSelectAll(event: Event): void {
    const checkbox = event.target as HTMLInputElement
    const selected: boolean = checkbox.checked
    // this.preparedSkills.forEach(skill => skill.selected = selected)
    this.selectAllSelected.emit(selected)
    this.rowSelected.emit(this.getSelectedCollections())
  }

  onRowToggle(skill: ICollectionSummary): void {
    // const maybeFound = this.preparedSkills.find(s => s.skill === skill)
    // if (maybeFound) {
    //   maybeFound.selected = !maybeFound.selected
    // }
    this.rowSelected.emit(this.getSelectedCollections())
  }
}
