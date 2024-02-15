import { Component, Input } from "@angular/core"

import { TableActionDefinition } from "../../table/skills-library-table/has-action-definitions"

@Component({
  selector: "app-vertical-action-bar-item",
  templateUrl: "./vertical-action-bar-item.component.html",
  styleUrls: [
    "./vertical-action-bar-item.component.scss",
    "../../table/skills-library-table/action-bar-item.components.scss"
  ]
})
export class VerticalActionBarItemComponent {

  @Input()
  action?: TableActionDefinition
  @Input()
  data?: any

}
