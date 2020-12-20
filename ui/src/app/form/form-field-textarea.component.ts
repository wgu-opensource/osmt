import {Component, Input, OnInit, Output} from "@angular/core"
import {AbstractControl, FormControl} from "@angular/forms";
import {FormField} from "./form-field.component";
import {Subject} from "rxjs";


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
        <textarea id="formfield-{{name}}"
                  [formControl]="control" [attr.placeholder]="includePlaceholder ? placeholder : null"
                  (blur)="blur.next($event)"
        ></textarea>
      </div>
    </app-formfield>`
})
export class FormFieldTextArea extends FormField implements OnInit {

  @Output() blur = new Subject<FocusEvent>()

  constructor() {
    super()
  }

  ngOnInit(): void {
  }
}
