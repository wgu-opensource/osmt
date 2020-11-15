import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {ApiSortOrder} from "../richskill/ApiSkill"
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
  @Input() currentSort: ApiSortOrder | undefined = undefined
  @Input() rowActions: TableActionDefinition[] = []

  @Output() columnSorted = new EventEmitter<ApiSortOrder>()

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
    } else if (column.toLowerCase() === "category") {
      if (ascending) {
        this.currentSort = ApiSortOrder.SkillAsc
      } else {
        this.currentSort = ApiSortOrder.SkillDesc
      }
    }
    this.columnSorted.emit(this.currentSort)
  }

}
