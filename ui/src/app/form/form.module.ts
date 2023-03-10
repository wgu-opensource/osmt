import {NgModule} from "@angular/core"
import {JobcodeSingleSelectComponent} from "./form-field-search-select"
import {FormFieldSearchSelectJobcodeComponent} from "../form"
import {FormsModule, ReactiveFormsModule} from "@angular/forms"
import {FormField} from "./form-field.component"
import {CommonModule} from "@angular/common"
import {FormFieldSearchMultiSelectComponent} from "./form-field-search-select"
import {FormFieldSearchSelectComponent} from "./form-field-search-select"
import {FormFieldSubmit} from "./form-field-submit.component"
import {FormFieldText} from "./form-field-text.component"
import {FormFieldTextArea} from "./form-field-textarea.component"

@NgModule({
  declarations: [
    FormField,
    FormFieldSubmit,
    FormFieldText,
    FormFieldTextArea,
    FormFieldSearchSelectJobcodeComponent,
    FormFieldSearchMultiSelectComponent,
    FormFieldSearchSelectComponent,
    JobcodeSingleSelectComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule
  ],
  exports: [
    FormField,
    FormFieldSubmit,
    FormFieldText,
    FormFieldTextArea,
    FormFieldSearchSelectJobcodeComponent,
    FormFieldSearchMultiSelectComponent,
    FormFieldSearchSelectComponent,
    JobcodeSingleSelectComponent
  ]
})
export class FormModule {
}
