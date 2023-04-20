import {Component, EventEmitter, Input, Output, QueryList, ViewChildren} from "@angular/core"
import {ApiCategory, KeywordSortOrder} from "../ApiCategory"
import {ISelectOption} from "../../table/label/select/select-label.component"
import {CategoryTableRowComponent} from "./row/category-table-row.component"

@Component({
  selector: "app-category-table",
  templateUrl: "./category-table.component.html"
})
export class CategoryTableComponent {
  @ViewChildren(CategoryTableRowComponent) rowReferences: QueryList<CategoryTableRowComponent> | undefined = undefined

  @Input() items: ApiCategory[] = []
  @Input() sortOrder: KeywordSortOrder = KeywordSortOrder.KeywordAsc
  @Output() sortOrderSelected = new EventEmitter<KeywordSortOrder>()

  readonly KeywordSortOrder = KeywordSortOrder

  readonly mobileSortOptionMap: Map<KeywordSortOrder, ISelectOption<KeywordSortOrder>> = new Map(
    [ KeywordSortOrder.KeywordAsc,
      KeywordSortOrder.KeywordDesc,
      KeywordSortOrder.SkillCountAsc,
      KeywordSortOrder.SkillCountDesc
    ].map(sortOrder => [sortOrder, { label: getLabelForSortOrder(sortOrder), value: sortOrder }])
  )

  get mobileSortOrderOptions(): ISelectOption<KeywordSortOrder>[] {
    return Array.from(this.mobileSortOptionMap.values())
  }

  get mobileSortOrderOption() {
    return this.mobileSortOptionMap.get(this.sortOrder)
  }

  handleSortOrderSelected(sortOrder: any) {
    this.sortOrderSelected.emit(sortOrder)
  }

  handleMobileSortOrderSelected(selection: ISelectOption<KeywordSortOrder>) {
    this.sortOrderSelected.emit(selection.value)
  }
}

export function getLabelForSortOrder(sortOrder: KeywordSortOrder): string {
  switch (sortOrder) {
    case KeywordSortOrder.KeywordAsc: return "Category name (ascending)"
    case KeywordSortOrder.KeywordDesc: return "Category name (descending)"
    case KeywordSortOrder.SkillCountAsc: return "Skill count (ascending)"
    case KeywordSortOrder.SkillCountDesc: return "Skill count (descending)"
    default: return ""
  }
}
