import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {ApiSkillSortOrder} from "../../richskill/ApiSkill"

@Component({
  selector: "app-table-header",
  templateUrl: "./table-header.component.html"
})
export class TableHeaderComponent implements OnInit {

  @Input() numberSelected = 0
  @Input() currentSort: ApiSkillSortOrder | undefined = undefined

  @Output() columnChanged = new EventEmitter<ApiSkillSortOrder>()
  @Output() selectAllChanged = new EventEmitter<boolean>()

  chevronIcon = SvgHelper.path(SvgIcon.CHEVRON)
  checkIcon = SvgHelper.path(SvgIcon.CHECK)


  constructor() { }

  ngOnInit(): void {
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
    this.columnChanged.emit(this.currentSort)
  }

  handleSelectAll(event: Event): void {
    const checkbox = event.target as HTMLInputElement
    this.selectAllChanged.emit(checkbox.checked)
  }
}
