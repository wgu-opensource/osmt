import { NgModule } from "@angular/core"
import { CommonModule } from "@angular/common"
import { FormsModule, ReactiveFormsModule } from "@angular/forms"
import { FilterChipsComponent } from "./filter-chips/filter-chips.component"
import { FilterDropdownComponent } from "./filter-drop-down/filter-dropdown.component"
import { SearchMultiSelectComponent } from "./search-multi-select/search-multi-select.component"

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
