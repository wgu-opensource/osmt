import { Component, Input, OnInit } from '@angular/core';
import { AbstractControl, FormControl } from '@angular/forms';
import { FormField } from './form-field.component';

@Component({
  selector: 'app-formfield-text',
  templateUrl: './form-field-text.component.html',
})
export class FormFieldText extends FormField implements OnInit {
  @Input() control: FormControl = new FormControl('');
  @Input() label = '';
  @Input() placeholder = '';
  @Input() errorMessage = '';
  @Input() helpMessage = '';
  @Input() required = false;
  @Input() name = '';
  @Input() includePlaceholder = true;

  constructor() {
    super();
  }

  ngOnInit(): void {}
}
