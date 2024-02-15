import { NgModule } from "@angular/core"
import { CommonModule } from "@angular/common"
import { FormsModule, ReactiveFormsModule } from "@angular/forms"
import {
  FilterChipsComponent,
  FilterDropdownComponent,
  SearchMultiSelectComponent
} from "@shared/."

@NgModule({
  declarations: [
    FilterDropdownComponent,
    SearchMultiSelectComponent,
    FilterChipsComponent
  ],
  exports: [
    FilterDropdownComponent,
    SearchMultiSelectComponent,
    FilterChipsComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule
  ]
})
export class SharedModule { }
