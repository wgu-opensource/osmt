import {NgModule} from "@angular/core"
import {CommonModule} from "@angular/common"
import { SelectAllComponent } from "./select-all/select-all.component"

@NgModule({
  declarations: [
    SelectAllComponent
  ],
  exports: [
    SelectAllComponent
  ],
  imports: [
    CommonModule
  ]
})
export class OsmtTableModule {
}
