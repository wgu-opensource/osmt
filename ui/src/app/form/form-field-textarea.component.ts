import {Component, Input, OnInit} from "@angular/core"
import {AbstractControl, FormControl} from "@angular/forms";
import {FormField} from "./form-field.component";


@Component({
  selector: "app-formfield-textarea",
  templateUrl: "./form-field-textarea.component.html"
})
export class FormFieldTextArea implements OnInit {

  @Input() control: FormControl = new FormControl("")
  @Input() label: string = ""
  @Input() placeholder: string = ""
  @Input() errorMessage: string = ""
  @Input() helpMessage: string = ""
  @Input() required: boolean = false

  constructor() {
  }

  ngOnInit(): void {
  }
}
