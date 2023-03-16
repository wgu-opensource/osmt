import {NgModule} from "@angular/core"
import {FormsModule, ReactiveFormsModule} from "@angular/forms"
import {CommonModule} from "@angular/common"
import {FormField} from "./form-field.component"
import {FormFieldJobCodeSearchSelect} from "./form-field-search-select"
import {FormFieldJobCodeSearchMultiSelect} from "./form-field-search-select"
import {FormFieldKeywordSearchSelect} from "./form-field-search-select"
import {FormFieldKeywordSearchMultiSelect} from "./form-field-search-select"
import {FormFieldSubmit} from "./form-field-submit.component"
import {FormFieldText} from "./form-field-text.component"
import {FormFieldTextArea} from "./form-field-textarea.component"

@NgModule({
  declarations: [
    FormField,
    FormFieldSubmit,
    FormFieldText,
    FormFieldTextArea,
    FormFieldJobCodeSearchSelect,
    FormFieldJobCodeSearchMultiSelect,
    FormFieldKeywordSearchSelect,
    FormFieldKeywordSearchMultiSelect
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
    FormFieldJobCodeSearchSelect,
    FormFieldJobCodeSearchMultiSelect,
    FormFieldKeywordSearchSelect,
    FormFieldKeywordSearchMultiSelect
  ]
})
export class FormModule {
}
