import {Component, Input, OnInit} from "@angular/core"
import {AbstractControl, FormControl} from "@angular/forms";


@Component({
  selector: "app-formfield",
  templateUrl: "./form-field.component.html"
})
export class FormField implements OnInit {

  @Input() control: FormControl = new FormControl()
  @Input() label: string = ""
  @Input() placeholder: string = ""
  @Input() errorMessage: string = ""
  @Input() helpMessage: string = ""

  constructor() {
  }

  ngOnInit(): void {
  }
}
