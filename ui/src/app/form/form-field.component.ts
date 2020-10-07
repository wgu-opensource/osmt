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
  @Input() required: boolean = false
  @Input() name: string = ""

  constructor() {
  }

  ngOnInit(): void {
  }

  isError(): boolean {
    return (this.control.dirty || this.control.touched) && this.control.invalid
  }
}
