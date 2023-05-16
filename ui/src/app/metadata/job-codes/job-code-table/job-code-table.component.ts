import { AfterViewInit, Component, Input, QueryList, ViewChildren } from "@angular/core"
import { AbstractTableComponent } from "../../../table/abstract-table.component"
import { JobCodeListRowComponent } from "../job-code-list-row/job-code-list-row.component"
import { IJobCode } from "../Jobcode"

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
}

