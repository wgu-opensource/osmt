import {Component, EventEmitter, Input, Output} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../core/SvgHelper"

export interface ISelectOption<TValue> {
  label: string
  value: TValue
}

@Component({
  selector: "app-label-select",
  templateUrl: "./select-label.component.html"
})
export class SelectLabelComponent {
  @Input() options?: ISelectOption<any>[]
  @Input() selected?: ISelectOption<any>
  @Output() onSelection: EventEmitter<ISelectOption<any>> = new EventEmitter<ISelectOption<any>>()

  readonly chevronIcon = SvgHelper.path(SvgIcon.CHEVRON)

  isOptionSelected(option: ISelectOption<any>): boolean {
    return  option === this.selected
  }

  handleOptionSelected(): void {
    this.onSelection.emit(this.selected)
  }
}
