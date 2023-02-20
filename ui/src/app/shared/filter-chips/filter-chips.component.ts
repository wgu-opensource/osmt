import {Component, EventEmitter, Input, Output} from "@angular/core"
import {FormControl} from "@angular/forms"

@Component({
  selector: "app-filter-chips",
  templateUrl: "./filter-chips.component.html",
  styleUrls: ["./filter-chips.component.scss"]
})
export class FilterChipsComponent {

  @Input()
  name?: string
  @Input()
  control?: FormControl
  @Output()
  remove: EventEmitter<boolean> = new EventEmitter<boolean>()

  onRemoveChip(chipText: string): void {
    const values = this.control?.value
    const index = this.control?.value?.findIndex((i: string) => i === chipText) ?? 0
    if (index >= 0) {
      values.splice(index, 1)
      this.control?.patchValue(values)
      this.remove.emit(true)
    }
  }

}
