import {Component, Input, OnInit} from "@angular/core"
import {AbstractControl, FormControl} from "@angular/forms"


@Component({
  selector: "app-formfield",
  templateUrl: "./form-field.component.html"
})
export class FormField<T = string> implements OnInit {

  @Input() control = new FormControl()
  @Input() label = ""
  @Input() placeholder = ""
  @Input() includePlaceholder = true
  @Input() errorMessage = ""
  @Input() helpMessage = ""
  @Input() required = false
  @Input() name = ""

  constructor() {
  }

  get valueFromControl(): T | null {
    return this.control.value
  }

  set value(v: T) {
    this.control.setValue(v)
  }

  clearField(): void {
    this.control.setValue(null)
  }

  ngOnInit(): void {
  }

  isError(): boolean {
    return this.control && (this.control.dirty || this.control.touched) && this.control.invalid
  }
}
