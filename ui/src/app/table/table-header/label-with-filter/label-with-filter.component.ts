import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../core/SvgHelper"

export enum SortDirections {UP = "up", DOWN = "down"
}

@Component({
  selector: "app-label-with-filter",
  template: `
    <button class="m-tableLabel" data-table-filter (click)="onClick()">
      <span class="m-tableLabel-x-text">{{label}}</span>
      <span class="m-tableLabel-x-control">
        <div class="l-iconTransition" [class.l-iconTransition-is-flipped]="isSortAscending()">
            <svg class="l-iconTransition-x-icon t-icon">
                <use [attr.xlink:href]="chevronIcon"></use>
            </svg>
        </div>
    </span>
    </button>
  `
})
export class LabelWithFilterComponent implements OnInit {

  @Input() label = ""
  @Input() sortDirection: SortDirections | undefined = undefined

  @Output() emitSort: EventEmitter<SortDirections> = new EventEmitter<SortDirections>()

  chevronIcon = SvgHelper.path(SvgIcon.CHEVRON)

  constructor() { }

  ngOnInit(): void {
  }

  isSortAscending(): boolean {
    return this.sortDirection === SortDirections.UP
  }

  onClick(): void {
    if (!this.sortDirection || this.sortDirection === SortDirections.UP) {
      this.emitSort.emit(SortDirections.DOWN)
      console.log("sorted " + SortDirections.DOWN)
    } else if (this.sortDirection === SortDirections.DOWN) {
      this.emitSort.emit(SortDirections.UP)
      console.log("sorted " + SortDirections.UP)
    }
    console.log("previously " + this.sortDirection)
  }

}
