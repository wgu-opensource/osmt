import {Component} from "@angular/core"
import {ApiSkillSummary} from "../../richskill/ApiSkillSummary"
import {AbstractTableComponent} from "../abstract-table.component"

@Component({
  selector: "app-skill-table",
  templateUrl: "./skill-table.component.html"
})
export class SkillTableComponent extends AbstractTableComponent<ApiSkillSummary> {

  constructor() {
    super()
  }

  shiftSelection(item: ApiSkillSummary): void {
    if (this.selectedItems.size > 0 && this.isShiftPressed) {
      const firstSelect = this.selectedItems.values().next().value
      const start = this.items.findIndex(i => i.uuid === firstSelect.uuid)
      const end = this.items.findIndex((i: ApiSkillSummary) => i.uuid === item.uuid)
      const selectedWithShift = end > start ? this.items.slice(start, end + 1) : this.items.slice(end, start + 1).reverse()
      this.selectedItems.clear()
      selectedWithShift.forEach(i => this.selectedItems.add(i))
    }
  }

}
