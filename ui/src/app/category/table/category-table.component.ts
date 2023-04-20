import {Component, EventEmitter, Input, Output, QueryList, ViewChildren} from "@angular/core"
import {ApiCategory, KeywordSortOrder} from "../ApiCategory"
import {CategoryTableRowComponent} from "./row/category-table-row.component"

@Component({
  selector: "app-category-table",
  templateUrl: "./category-table.component.html"
})
export class CategoryTableComponent {
  @ViewChildren(CategoryTableRowComponent) rowReferences: QueryList<CategoryTableRowComponent> | undefined = undefined

  @Input() items: ApiCategory[] = []
  @Input() sortOrder: KeywordSortOrder = KeywordSortOrder.KeywordAsc
  @Output() columnSorted = new EventEmitter<KeywordSortOrder>()

  readonly KeywordSortOrder = KeywordSortOrder

  onSortOrderSelected(sortOrder: any) {
    this.columnSorted.emit(sortOrder)
  }
}
