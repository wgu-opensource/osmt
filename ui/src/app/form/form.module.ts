import { NgModule } from "@angular/core"
import { CommonModule } from "@angular/common"
import { FormFieldSearchMultiSelectComponent } from "./form-field-search-select/mulit-select/form-field-search-multi-select.component"
import { SearchMultiSelectComponent } from "./search-multi-select/search-multi-select.component"
import { FormField } from "./form-field.component"
import { FormFieldSubmit } from "./form-field-submit.component"
import { FormFieldText } from "./form-field-text.component"
import { FormFieldTextArea } from "./form-field-textarea.component"
import { FormsModule, ReactiveFormsModule } from "@angular/forms"

@NgModule({
  declarations: [
    FormFieldSearchMultiSelectComponent,
    SearchMultiSelectComponent,
    FormField,
    FormFieldSubmit,
    FormFieldText,
    FormFieldTextArea
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule
  ],
  exports: [
    FormFieldSearchMultiSelectComponent,
    SearchMultiSelectComponent,
    FormField,
    FormFieldSubmit,
    FormFieldText,
    FormFieldTextArea
  ]
})
export class FormModule {
}
