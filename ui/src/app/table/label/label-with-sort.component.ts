import {Component, EventEmitter, Input, Output} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"

@Component({
  selector: "app-label-with-sort",
  template: `
    <button class="m-tableLabel" data-table-filter (click)="onClick()">
      <span class="m-tableLabel-x-text"><ng-content></ng-content></span>
      <span class="m-tableLabel-x-control">
        <div class="l-iconTransition" [class.l-iconTransition-is-flipped]="flipIcon">
            <svg class="l-iconTransition-x-icon t-icon">
                <use [attr.xlink:href]="chevronIcon"></use>
            </svg>
        </div>
    </span>
    </button>
  `
})
export class LabelWithSortComponent {
  @Input() sortOrderAsc?: any
  @Input() sortOrderDesc?: any
  @Input() currentSortOrder?: any
  @Output() sortOrderSelected: EventEmitter<any> = new EventEmitter<any>()

  chevronIcon = SvgHelper.path(SvgIcon.CHEVRON)

  get isSortAsc(): boolean {
    return  this.currentSortOrder === this.sortOrderAsc
  }

  get isSortDesc(): boolean {
    return this.currentSortOrder === this.sortOrderDesc
  }

  get flipIcon(): boolean {
    return  this.isSortAsc
  }

  onClick(): void {
    if (this.isSortAsc) {
      this.sortOrderSelected.emit(this.sortOrderDesc)
    } else {
      this.sortOrderSelected.emit(this.sortOrderAsc)
    }
  }
}
