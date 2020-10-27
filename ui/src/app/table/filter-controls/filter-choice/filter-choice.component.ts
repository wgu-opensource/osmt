import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../core/SvgHelper"

@Component({
  selector: "app-filter-choice",
  template: `
    <div class="m-choice">
      <div class="m-choice-x-body">
        <label [for]="inputId">{{label}}</label>
      </div>
      <div class="m-choice-x-input">
        <div class="m-checkbox">
          <input type="checkbox" [id]="inputId" name="checkbox" (change)="onChange($event)" [attr.checked]="checked ? true : null">
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

  @Input() checked = false

  @Output() changeEmitter: EventEmitter<boolean> = new EventEmitter<boolean>()

  @Input() id?: string

  checkIcon = SvgHelper.path(SvgIcon.CHECK)

  constructor() { }

  ngOnInit(): void {
  }

  onChange(event: Event): void {
    const checkbox = event.target as HTMLInputElement
    this.changeEmitter.emit(checkbox.checked)
  }

  get inputId(): string {
    const scrubbed = this.label.toLowerCase()
    return (this.id) ? this.id : `checkbox-${scrubbed}`
  }
}
