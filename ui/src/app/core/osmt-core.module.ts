import { CommonModule } from "@angular/common"
import { NgModule } from "@angular/core"

import { DropUpMenuComponent } from "./drop-up-menu/drop-up-menu.component"
import { HorizontalActionBarItemComponent } from "./horizontal-action-bar-item/horizontal-action-bar-item.component"
import { VerticalActionBarItemComponent } from "./vertical-action-bar-item/vertical-action-bar-item.component"

@NgModule({
  declarations: [
    DropUpMenuComponent,
    HorizontalActionBarItemComponent,
    VerticalActionBarItemComponent
  ],
  imports: [
    CommonModule
  ],
  exports: [
    DropUpMenuComponent,
    HorizontalActionBarItemComponent,
    VerticalActionBarItemComponent
  ]
})
export class OsmtCoreModule { }
