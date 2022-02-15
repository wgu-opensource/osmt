import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {FormControl} from "@angular/forms";
import {FormField} from "./form-field.component";
import {SvgHelper, SvgIcon} from "../core/SvgHelper";


export interface IChoice {
  id: string|number|undefined
  name: string
  label: string
  initiallySelected?: boolean
}

@Component({
  selector: "app-formfield-choice",
  templateUrl: "./form-field-choice.component.html"
})
export class FormFieldChoiceComponent extends FormField implements IChoice {

  @Input() control: FormControl = new FormControl("")
  @Input() id: string|number|undefined
  @Input() label: string = ""
  @Input() name: string = ""
  @Input() isSelected: boolean = false
  @Input() helpMessage: string = ""
  @Input() errorMessage: string = ""
  @Input() required: boolean = false
  @Output() onSelected: EventEmitter<string|number> = new EventEmitter<string|number>()
  @Output() onDeselected: EventEmitter<string|number> = new EventEmitter<string|number>()

  checkIcon = SvgHelper.path(SvgIcon.CHECK)

  constructor() {
    super()
  }

  handleOnChange(event: Event): void {
    if (this.isSelected) {
      this.onDeselected.emit(this.id)
    }
    else {
      this.onSelected.emit(this.id)
    }
  }
}
