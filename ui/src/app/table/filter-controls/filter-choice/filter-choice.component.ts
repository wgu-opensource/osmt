import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../core/SvgHelper"

@Component({
  selector: "app-filter-choice",
  template: `
    <div class="m-choice">
      <div class="m-choice-x-body">
        <label for="checkbox">{{label}}</label>
      </div>
      <div class="m-choice-x-input">
        <div class="m-checkbox">
          <input type="checkbox" id="checkbox" name="checkbox" [checked]="isChecked" (change)="onChange($event)">
          <div class="m-checkbox-x-icon">
            <svg class="t-icon" aria-hidden="true">
              <use [attr.xlink:href]="checkIcon"></use>
            </svg>
          </div>
        </div>
      </div>
    </div>
  `})
export class FilterChoiceComponent implements OnInit {

  @Input() label = ""
  @Input() isChecked = false

  @Output() changeEmitter: EventEmitter<boolean> = new EventEmitter<boolean>()

  checkIcon = SvgHelper.path(SvgIcon.CHECK)

  constructor() { }

  ngOnInit(): void {
  }

  onChange(event: Event): void {
    const checkbox = event.target as HTMLInputElement
    this.changeEmitter.emit(checkbox.checked)
  }
}
