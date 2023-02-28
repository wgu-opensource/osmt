import { NgModule } from "@angular/core"
import { CommonModule } from "@angular/common"

import { VerticalActionBarItemComponent } from "./vertical-action-bar-item/vertical-action-bar-item.component"
import { HorizontalActionBarItemComponent } from "./horizontal-action-bar-item/horizontal-action-bar-item.component"
import { DropUpMenuComponent } from "./drop-up-menu/drop-up-menu.component"



@NgModule({
  declarations: [
    VerticalActionBarItemComponent,
    HorizontalActionBarItemComponent,
    DropUpMenuComponent
  ],
  imports: [
    CommonModule
  ],
  exports: [
    VerticalActionBarItemComponent,
    HorizontalActionBarItemComponent,
    DropUpMenuComponent
  ]
})
export class OsmtCoreModule { }
