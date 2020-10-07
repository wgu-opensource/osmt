import {Component, Input, OnInit} from "@angular/core"
import {AbstractControl, FormControl} from "@angular/forms";
import {FormField} from "./form-field.component";


@Component({
  selector: "app-formfield-text",
  templateUrl: "./form-field-text.component.html"
})
export class FormFieldText implements OnInit {

  @Input() control: FormControl = new FormControl("")
  @Input() label: string = ""
  @Input() placeholder: string = ""
  @Input() errorMessage: string = ""
  @Input() helpMessage: string = ""
  @Input() required: boolean = false
  @Input() name: string = ""

  constructor() {
  }

  ngOnInit(): void {
  }
}
