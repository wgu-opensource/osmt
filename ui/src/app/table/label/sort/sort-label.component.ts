import {Component, EventEmitter, Input, Output} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../core/SvgHelper"

@Component({
  selector: "app-label-sort",
  templateUrl: "./sort-label.component.html"
})
export class SortLabelComponent {
  @Input() sortOrderAsc?: any
  @Input() sortOrderDesc?: any
  @Input() selected?: any
  @Output() onSelection: EventEmitter<any> = new EventEmitter<any>()

  readonly chevronIcon = SvgHelper.path(SvgIcon.CHEVRON)

  get isAscSelected(): boolean {
    return  this.selected === this.sortOrderAsc
  }

  get isDescSelected(): boolean {
    return this.selected === this.sortOrderDesc
  }

  get flipIcon(): boolean {
    return  this.isAscSelected
  }

  handleClick(): void {
    if (this.isAscSelected) {
      this.onSelection.emit(this.sortOrderDesc)
    } else {
      this.onSelection.emit(this.sortOrderAsc)
    }
  }
}
