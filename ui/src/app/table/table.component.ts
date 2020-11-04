import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {ApiSkillSortOrder} from "../richskill/ApiSkill"
import {Observable} from "rxjs"
import {SkillWithMetadata} from "../richskill/list/skill-list-row.component"
import {IApiSkillSummary} from "../richskill/ApiSkillSummary"
import {TableActionDefinition} from "./has-action-definitions";
import {SvgHelper, SvgIcon} from "../core/SvgHelper";

/**
 * Implement row components to hold datasets and figure out how to dynamically pass and use them
 */
@Component({
  selector: "app-table",
  templateUrl: "./table.component.html"
})
export class TableComponent implements OnInit {

  @Input() skills: IApiSkillSummary[] = []
  @Input() currentSort: ApiSkillSortOrder | undefined = undefined
  @Input() rowActions: TableActionDefinition[] = []

  // Any time a row is selected, broadcast out the list of currently selected skills from preparedSkills
  @Output() rowSelected: EventEmitter<IApiSkillSummary[]> = new EventEmitter<IApiSkillSummary[]>()
  @Output() columnSorted = new EventEmitter<ApiSkillSortOrder>()
  @Output() selectAllSelected = new EventEmitter<boolean>()
  @Input() selectAllCount?: number

  // handles the inner state of the loaded skills
  preparedSkills: SkillWithMetadata[] = []

  checkIcon = SvgHelper.path(SvgIcon.CHECK)

  constructor() { }

  ngOnInit(): void {
    this.preparedSkills = this.skills?.map<SkillWithMetadata>(skill => ({skill, selected: false})) ?? []
  }

  getSelectedSkills(): IApiSkillSummary[] {
    return this.preparedSkills.filter(row => row.selected).map(row => row.skill)
  }

  numberOfSelected(): number {
    return this.preparedSkills.filter(pk => pk.selected).length
  }
  getSelectAllCount(): number {
    return (this.selectAllCount !== undefined) ? this.selectAllCount : this.preparedSkills.length
  }

  // Every time a row is toggled, emit the current list of all selected rows
  onRowToggle(skill: IApiSkillSummary): void {
    const maybeFound = this.preparedSkills.find(s => s.skill === skill)
    if (maybeFound) {
      maybeFound.selected = !maybeFound.selected
    }
    this.rowSelected.emit(this.getSelectedSkills())

  }

  handleSelectAll(event: Event): void {
    const checkbox = event.target as HTMLInputElement
    const selected: boolean = checkbox.checked
    this.preparedSkills.forEach(skill => skill.selected = selected)
    this.selectAllSelected.emit(selected)
    this.rowSelected.emit(this.getSelectedSkills())
  }


  getCategorySort(): boolean | undefined {
    if (this.currentSort) {
      switch (this.currentSort) {
        case ApiSkillSortOrder.CategoryAsc: return true
        case ApiSkillSortOrder.CategoryDesc: return false
      }
    }
    return undefined
  }

  getSkillSort(): boolean | undefined {
    if (this.currentSort) {
      switch (this.currentSort) {
        case ApiSkillSortOrder.NameAsc: return true
        case ApiSkillSortOrder.NameDesc: return false
      }
    }
    return undefined
  }

  sortColumn(column: string, ascending: boolean): void {
    if (column.toLowerCase() === "name") {
      if (ascending) {
        this.currentSort = ApiSkillSortOrder.NameAsc
      } else {
        this.currentSort = ApiSkillSortOrder.NameDesc
      }
    } else if (column.toLowerCase() === "category") {
      if (ascending) {
        this.currentSort = ApiSkillSortOrder.CategoryAsc
      } else {
        this.currentSort = ApiSkillSortOrder.CategoryDesc
      }
    }
    this.columnSorted.emit(this.currentSort)
  }

}
