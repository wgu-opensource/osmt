import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {TableActionDefinition} from "../../../table/skills-library-table/has-action-definitions"
import {SvgHelper, SvgIcon} from "../../../core/SvgHelper"
import {ApiJobCode, IJobCode} from "../Jobcode"

@Component({
  // tslint:disable-next-line:component-selector
  selector: "[app-job-code-list-row]",
  templateUrl: "./job-code-list-row.component.html"
})
export class JobCodeListRowComponent implements OnInit {
  @Input() jobcode?: ApiJobCode
  @Input() id = "job-code-list-row"
  @Input() isSelected = false
  @Input() rowActions: TableActionDefinition[] = []

  @Output() rowSelected = new EventEmitter<IJobCode>()
  @Output() focusActionBar = new EventEmitter<void>()
  checkIcon = SvgHelper.path(SvgIcon.CHECK)

  constructor() { }

  ngOnInit(): void {
  }

  selected(): void {
    this.rowSelected.emit(this.jobcode)
  }

  get singleRowAction(): boolean {
    return this.rowActions.length === 1
  }

  handleClick(action: TableActionDefinition): boolean {
    action.fire(this.jobcode)
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
}
