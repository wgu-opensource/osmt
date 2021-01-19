import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {ApiSortOrder} from "../../richskill/ApiSkill";


@Component({
  selector: "app-label-with-select",
  template: `
    <div class="m-tableLabel">
      <span class="m-tableLabel-x-text">{{label}}</span>
      <div class="m-tableLabel-x-control">
        <!--{{render '@m-selectsmall'}}-->
        <div class="m-selectSmall">
          <select class="m-selectSmall-x-select" (change)="handleChange($event)">
            <option *ngFor="let option of options | keyvalue" [value]="option.key" [attr.selected]="option.key === currentSort ? '' : null">{{option.value}}</option>
          </select>
          <div class="m-selectSmall-x-icon">
            <svg aria-hidden="true">
              <use [attr.xlink:href]="chevronIcon"></use>
            </svg>
          </div>
        </div>

      </div>
    </div>
  `
})
export class LabelWithSelectComponent implements OnInit {

  @Input() label = ""
  @Input() currentSort: ApiSortOrder = ApiSortOrder.NameAsc
  @Input() options: {[s: string]: string} = {}
  @Output() sortChanged: EventEmitter<ApiSortOrder> = new EventEmitter<ApiSortOrder>()

  chevronIcon = SvgHelper.path(SvgIcon.CHEVRON)

  constructor() { }

  ngOnInit(): void {
  }

  handleChange(event: Event): void {
    const target = event.target as HTMLSelectElement
    const value: ApiSortOrder = target.value as ApiSortOrder
    this.sortChanged.emit(value)
  }
}
