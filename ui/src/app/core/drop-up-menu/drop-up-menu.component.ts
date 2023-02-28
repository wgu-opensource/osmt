import {Component, Input} from "@angular/core"
import {TableActionDefinition} from "../../table/skills-library-table/has-action-definitions"

@Component({
  selector: "app-drop-up-menu",
  templateUrl: "./drop-up-menu.component.html",
  styleUrls: ["../../table/skills-library-table/action-bar-item.components.scss"]
})
export class DropUpMenuComponent {

  @Input()
  action?: TableActionDefinition

}
