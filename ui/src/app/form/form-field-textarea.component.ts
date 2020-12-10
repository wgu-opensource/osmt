import {Component, Input, OnInit} from "@angular/core"
import {AbstractControl, FormControl} from "@angular/forms";
import {FormField} from "./form-field.component";


@Component({
  selector: "app-formfield-textarea",
  template: `
    <app-formfield
      [control]="control"
      [label]="label"
      [placeholder]="placeholder"
      [errorMessage]="errorMessage"
      [helpMessage]="helpMessage"
      [required]="required"
      [name]="name"
    >
      <div class="m-text" [class.m-text-is-error]="isError()">
        <textarea [formControl]="control" [attr.placeholder]="includePlaceholder ? placeholder : null" id="formfield-{{name}}"></textarea>
      </div>
    </app-formfield>`
})
export class FormFieldTextArea extends FormField implements OnInit {

  constructor() {
    super()
  }

  ngOnInit(): void {
  }
}
