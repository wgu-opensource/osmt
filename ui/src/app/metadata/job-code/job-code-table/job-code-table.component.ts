import { AfterViewInit, Component, Input, QueryList, ViewChildren } from "@angular/core"
import { AbstractTableComponent } from "../../../table/abstract-table.component"
import { JobCodeListRowComponent } from "../job-code-list-row/job-code-list-row.component"
import { IJobCode } from "../Jobcode"
import { ApiSortOrder } from "../../../richskill/ApiSkill"

@Component({
  selector: "app-job-code-table",
  templateUrl: "./job-code-table.component.html"
})
export class JobCodeTableComponent extends AbstractTableComponent<IJobCode> implements AfterViewInit {

  @ViewChildren(JobCodeListRowComponent) rowReferences: QueryList<JobCodeListRowComponent> | undefined = undefined

  @Input() allowSorting = false

  ngAfterViewInit(): void {
    if (this.rowReferences && this.rowReferences.length > 0) {
      this.rowReferences.first.focusFirstColumnInRow()
    }
  }

  sortColumn(column: string, ascending: boolean): void {
    if (column.toLowerCase() === "name") {
      this.currentSort = ascending ? ApiSortOrder.NameAsc : this.currentSort = ApiSortOrder.NameDesc
    } else if (column.toLowerCase() === "code") {
      this.currentSort = ascending ? ApiSortOrder.CodeAsc : ApiSortOrder.CodeDesc
    } else {
        this.currentSort = ascending ? ApiSortOrder.JobCodeLevelAsc : ApiSortOrder.JobCodeLevelDesc
    }
    this.columnSorted.emit(this.currentSort)
  }

  getCodeSort(): boolean | undefined {
    return this.currentSort == ApiSortOrder.CodeAsc;
  }

  getJobCodeLevelSort(): boolean | undefined {
    return this.currentSort == ApiSortOrder.JobCodeLevelAsc;
  }

}

