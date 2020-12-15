import {Component, Input, OnInit} from "@angular/core"
import {AbstractControl, FormControl} from "@angular/forms";


@Component({
  selector: "app-formfield",
  templateUrl: "./form-field.component.html"
})
export class FormField implements OnInit {

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

  get valueFromControl(): string {
    return this.control.value
  }

  ngOnInit(): void {
  }

  isError(): boolean {
    return this.control && (this.control.dirty || this.control.touched) && this.control.invalid
  }
}
