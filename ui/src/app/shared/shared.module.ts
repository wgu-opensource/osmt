import { NgModule } from "@angular/core"
import { CommonModule } from "@angular/common"
import { FilterComponent } from "./filter/filter.component"
import { FormsModule, ReactiveFormsModule } from "@angular/forms"
import {FormModule} from "../form/form.module"



@NgModule({
  declarations: [
    FilterComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    FormModule
  ]
})
export class SharedModule { }
