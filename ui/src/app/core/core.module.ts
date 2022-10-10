import { NgModule } from "@angular/core"
import { CommonModule } from "@angular/common"
import {AccordianComponent} from "./accordian.component";


@NgModule({
  imports: [
    CommonModule
  ],
  declarations: [
    AccordianComponent,
  ],
  exports: [
    AccordianComponent,
    CommonModule
  ],
  providers: [
  ]
})
export class CoreModule { }
