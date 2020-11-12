import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {ApiSkillSortOrder} from "../richskill/ApiSkill"
import {Observable} from "rxjs"
import {SkillWithMetadata} from "../richskill/list/skill-list-row.component"
import {IApiSkillSummary} from "../richskill/ApiSkillSummary"
import {TableActionDefinition} from "./skills-library-table/has-action-definitions"
import {SvgHelper, SvgIcon} from "../core/SvgHelper"

/**
 * Implement row components to hold datasets and figure out how to dynamically pass and use them
 */
@Component({
  selector: "app-abstract-table",
  template: ``
})
export class AbstractTableComponent implements OnInit {

  @Input() skills: IApiSkillSummary[] = []
  @Input() currentSort: ApiSkillSortOrder | undefined = undefined
  @Input() rowActions: TableActionDefinition[] = []

  @Output() columnSorted = new EventEmitter<ApiSkillSortOrder>()

  // handles the inner state of the loaded skills
  preparedSkills: SkillWithMetadata[] = []

  checkIcon = SvgHelper.path(SvgIcon.CHECK)

  constructor() { }

  ngOnInit(): void {
    this.preparedSkills = this.skills?.map<SkillWithMetadata>(skill => ({skill, selected: false})) ?? []
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
