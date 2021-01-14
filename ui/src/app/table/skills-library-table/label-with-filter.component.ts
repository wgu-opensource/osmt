import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"


@Component({
  selector: "app-label-with-filter",
  template: `
    <button class="m-tableLabel" data-table-filter (click)="handleClick()">
      <span class="m-tableLabel-x-text">{{label}}<ng-content *ngIf="label.length < 1"></ng-content></span>
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
  @Input() sortAscending: boolean | undefined = undefined

  @Output() sortChanged: EventEmitter<boolean> = new EventEmitter<boolean>()

  chevronIcon = SvgHelper.path(SvgIcon.CHEVRON)

  constructor() { }

  ngOnInit(): void {
  }

  isSortAscending(): boolean {
    return this.sortAscending === true
  }

  handleClick(): void {
    if (this.sortAscending === undefined || this.sortAscending) {
      this.sortChanged.emit(false)
    } else if (!this.sortAscending) {
      this.sortChanged.emit(true)
    }
  }
}
