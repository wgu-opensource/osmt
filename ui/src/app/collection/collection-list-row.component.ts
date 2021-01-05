import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {ApiCollectionSummary, ICollectionSummary} from "../richskill/ApiSkillSummary"
import {TableActionDefinition} from "../table/skills-library-table/has-action-definitions"
import {SvgHelper, SvgIcon} from "../core/SvgHelper"

@Component({
  // tslint:disable-next-line:component-selector
  selector: "[app-collection-list-row]",
  templateUrl: "./collection-list-row.component.html"
})
export class CollectionListRowComponent implements OnInit {
  @Input() collection?: ApiCollectionSummary
  @Input() id = "collection-list-row"
  @Input() isSelected = false
  @Input() rowActions: TableActionDefinition[] = []

  @Output() rowSelected = new EventEmitter<ICollectionSummary>()
  @Output() focusActionBar = new EventEmitter<void>()
  checkIcon = SvgHelper.path(SvgIcon.CHECK)

  constructor() { }

  ngOnInit(): void {
  }

  selected(): void {
    this.rowSelected.emit(this.collection)
  }

  get singleRowAction(): boolean {
    return this.rowActions.length === 1
  }

  handleClick(action: TableActionDefinition): boolean {
    action.fire(this.collection)
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
