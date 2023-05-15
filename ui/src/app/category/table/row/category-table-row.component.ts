import {Component, Input, OnInit} from "@angular/core"
import {ApiCategory} from "../../ApiCategory"

@Component({
  selector: "[app-category-table-row]",
  templateUrl: "./category-table-row.component.html"
})
export class CategoryTableRowComponent {
  @Input() category?: ApiCategory
  @Input() id = "category-list-row"

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
