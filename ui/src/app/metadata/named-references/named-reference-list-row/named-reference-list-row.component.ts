import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {TableActionDefinition} from "../../../table/skills-library-table/has-action-definitions"
import {SvgHelper, SvgIcon} from "../../../core/SvgHelper"
import {ApiNamedReference, INamedReference} from "../NamedReference"

@Component({
  // tslint:disable-next-line:component-selector
  selector: "[app-named-reference-list-row]",
  templateUrl: "./named-reference-list-row.component.html"
})
export class NamedReferenceListRowComponent implements OnInit {
  @Input() namedReference?: ApiNamedReference
  @Input() id = "named-reference-list-row"
  @Input() isSelected = false
  @Input() rowActions: TableActionDefinition[] = []

  @Output() rowSelected = new EventEmitter<INamedReference>()
  @Output() focusActionBar = new EventEmitter<void>()
  checkIcon = SvgHelper.path(SvgIcon.CHECK)

  constructor() { }

  ngOnInit(): void {
  }

  selected(): void {
    this.rowSelected.emit(this.namedReference)
  }

  get singleRowAction(): boolean {
    return this.rowActions.length === 1
  }

  handleClick(action: TableActionDefinition): boolean {
    action.fire(this.namedReference)
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
