import {Component, Input, OnInit} from "@angular/core";
import {TableActionDefinition} from "./has-action-definitions";

@Component({
  selector: "app-action-bar-item",
  templateUrl: "./action-bar-item.component.html"
})
export class ActionBarItemComponent implements OnInit {
  @Input() action: TableActionDefinition | undefined

  constructor() {
  }

  ngOnInit(): void {
  }

  iconPath(): string {
    return `/assets/images/svg-defs.svg#icon-${this.action?.icon}`
  }

  handleClick($event: MouseEvent): boolean {
    this.action?.fire()
    return false
  }
}
