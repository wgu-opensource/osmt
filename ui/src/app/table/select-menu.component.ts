import {Component, Input, OnInit} from "@angular/core"
import {HasActionDefinitions, TableActionDefinition} from "./has-action-definitions";

@Component({
  selector: "app-select-menu",
  templateUrl: "./select-menu.component.html"
})
export class SelectMenuComponent extends HasActionDefinitions implements OnInit {
  @Input() title?: string

  constructor() {
    super()
  }

  ngOnInit(): void {
  }

  handleClickTrigger(): boolean {
    return false
  }
}
