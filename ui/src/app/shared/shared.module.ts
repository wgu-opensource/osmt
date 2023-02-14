import { NgModule } from "@angular/core"
import { CommonModule } from "@angular/common"
import { FormsModule, ReactiveFormsModule } from "@angular/forms"
import { FormModule } from "../form/form.module"
import { FilterChipsComponent } from "./filter-chips/filter-chips.component"
import { FilterDropdownComponent } from "./filter-drop-down/filter-dropdown.component"



@NgModule({
  declarations: [
    FilterDropdownComponent,
    FilterChipsComponent
  ],
  exports: [
    FilterDropdownComponent,
    FilterChipsComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    FormModule
  ]
})
export class SharedModule { }
