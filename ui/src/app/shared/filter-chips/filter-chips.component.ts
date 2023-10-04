import {Component, EventEmitter, Input, Output} from "@angular/core"
import {FormControl} from "@angular/forms"
import { ApiJobCode } from "../../metadata/job-code/Jobcode"
import {ApiNamedReference} from "../../richskill/ApiSkill"
import {FilterSearchComponent} from "@shared/filter-search/filter-search.component"

@Component({
  selector: "app-filter-chips",
  templateUrl: "./filter-chips.component.html",
  styleUrls: ["./filter-chips.component.scss"]
})
export class FilterChipsComponent extends FilterSearchComponent {

  @Input()
  name?: string
  @Input()
  control?: FormControl
  @Output()
  remove: EventEmitter<boolean> = new EventEmitter<boolean>()

  onRemoveChip(chip: ApiJobCode | ApiNamedReference): void {
    const values = this.control?.value
    const index = this.control?.value?.findIndex((i: ApiJobCode | ApiNamedReference) => this.areResultsEqual(chip, i)) ?? 0
    if (index >= 0) {
      values.splice(index, 1)
      this.control?.patchValue(values)
      this.remove.emit(true)
    }
  }

}
