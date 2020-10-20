import {Component, Input, OnInit} from "@angular/core"
import {AbstractControl, FormControl} from "@angular/forms";
import {FormField} from "./form-field.component";


@Component({
  selector: "app-formfield-textarea",
  templateUrl: "./form-field-textarea.component.html"
})
export class FormFieldTextArea extends FormField implements OnInit {

  @Input() control: FormControl = new FormControl("")
  @Input() label: string = ""
  @Input() placeholder: string = ""
  @Input() errorMessage: string = ""
  @Input() helpMessage: string = ""
  @Input() required: boolean = false
  @Input() name: string = ""
  @Input() includePlaceholder: boolean = true

  constructor() {
    super()
  }

  ngOnInit(): void {
  }
}
