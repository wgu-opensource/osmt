import { Component, EventEmitter, Input, Output } from "@angular/core"
import { TableActionDefinition } from "../../../table/skills-library-table/has-action-definitions"
import { SvgHelper, SvgIcon } from "../../../core/SvgHelper"
import { ApiJobCode, IJobCode } from "../Jobcode"

@Component({
  // tslint:disable-next-line:component-selector
  selector: "[app-job-code-list-row]",
  templateUrl: "./job-code-list-row.component.html"
})
export class JobCodeListRowComponent {
  @Input() jobCode?: ApiJobCode
  @Input() id = "job-code-list-row"
  @Input() isSelected = false
  @Input() rowActions: TableActionDefinition[] = []

  @Output() rowSelected = new EventEmitter<IJobCode>()
  @Output() focusActionBar = new EventEmitter<void>()
  checkIcon = SvgHelper.path(SvgIcon.CHECK)

  constructor() { }

  selected(): void {
    this.rowSelected.emit(this.jobCode)
  }

  handleClick(action: TableActionDefinition): boolean {
    action.fire(this.jobCode)
    return false
  }

  focusFirstColumnInRow(): boolean {
    const ref = document.getElementById(`${this.id}-header-name`)
    if (ref) {
      ref.focus()
      return true
    } else {
      return false
    }
  }

  sortedParents(): IJobCode[] {
    return this.jobCode?.parents?.sort((a, b) => {
      if (a.code < b.code) {
        return -1
      } else if (a.code > b.code) {
        return 1
      }
      return 0
    }) ?? []
  }
}
