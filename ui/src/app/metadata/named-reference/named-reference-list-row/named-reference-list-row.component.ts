import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core"
import { TableActionDefinition } from "../../../table/skills-library-table/has-action-definitions"
import { SvgHelper, SvgIcon } from "../../../core/SvgHelper"
import { ApiNamedReference, NamedReferenceInterface } from "../NamedReference"
import { MetadataType } from "../../rsd-metadata.enum";

@Component({
  // tslint:disable-next-line:component-selector
  selector: "[app-named-reference-list-row]",
  templateUrl: "./named-reference-list-row.component.html"
})
export class NamedReferenceListRowComponent {
  @Input() namedReference?: ApiNamedReference
  @Input() id = "named-reference-list-row"
  @Input() isSelected = false
  @Input() rowActions: TableActionDefinition[] = []
  @Input() metadataSelected?: MetadataType


  @Output() rowSelected = new EventEmitter<NamedReferenceInterface>()
  @Output() focusActionBar = new EventEmitter<void>()
  checkIcon = SvgHelper.path(SvgIcon.CHECK)

  constructor() { }

  selected(): void {
    this.rowSelected.emit(this.namedReference)
  }

  handleClick(action: TableActionDefinition): boolean {
    action.fire(this.namedReference)
    return false
  }

  get isAlignmentSelected(): boolean {
    return this.metadataSelected == MetadataType.Alignment
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
