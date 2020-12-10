import {AfterViewInit, Component, Input, QueryList, ViewChildren} from "@angular/core"
import {ICollectionSummary} from "../richskill/ApiSkillSummary"
import {AbstractTableComponent} from "../table/abstract-table.component"
import {CollectionListRowComponent} from "./collection-list-row.component"

@Component({
  selector: "app-collection-table",
  templateUrl: "./collection-table.component.html"
})
export class CollectionTableComponent extends AbstractTableComponent<ICollectionSummary> implements AfterViewInit {

  @ViewChildren(CollectionListRowComponent) rowReferences: QueryList<CollectionListRowComponent> | undefined = undefined

  @Input() allowSorting = false

  ngAfterViewInit(): void {
    if (this.rowReferences && this.rowReferences.length > 0) {
      this.rowReferences.first.focusFirstColumnInRow()
    }
  }
}
