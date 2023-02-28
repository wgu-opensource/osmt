import {Component, Input, OnInit} from "@angular/core"
import {TableActionDefinition} from "../../table/skills-library-table/has-action-definitions"

@Component({
  selector: "app-horizontal-action-bar-item",
  templateUrl: "./horizontal-action-bar-item.component.html",
  styleUrls: ["./horizontal-action-bar-item.component.scss", "../../table/skills-library-table/action-bar-item.components.scss"]
})
export class HorizontalActionBarItemComponent {

  @Input()
  action?: TableActionDefinition

}
