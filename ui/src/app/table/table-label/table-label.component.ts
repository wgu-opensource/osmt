import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"

@Component({
  selector: "app-table-label",
  template: `
    <button class="m-tableLabel" data-table-filter (click)="onClick()">
      <span class="m-tableLabel-x-text">{{text}}</span>
      <span class="m-tableLabel-x-control">
        <div class="l-iconTransition" [class.l-iconTransition-is-flipped]="ascending">
            <svg class="l-iconTransition-x-icon t-icon">
                <use [attr.xlink:href]="iconChevron"></use>
            </svg>
        </div>
    </span>
    </button>
  `
})
export class TableLabelComponent implements OnInit {

  @Input() text = ""
  @Output() sortChangeFilter: EventEmitter<boolean> = new EventEmitter<boolean>()

  ascending = true

  iconChevron = SvgHelper.path(SvgIcon.CHEVRON)

  constructor() { }

  ngOnInit(): void {
  }

  onClick(): void {
    this.ascending = !this.ascending
    this.sortChangeFilter.emit(this.ascending)
  }

}
