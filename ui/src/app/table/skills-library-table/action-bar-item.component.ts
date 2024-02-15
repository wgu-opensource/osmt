import {Component, Input, OnInit} from "@angular/core";
import {TableActionDefinition} from "./has-action-definitions";

@Component({
  selector: "app-action-bar-item",
  templateUrl: "./action-bar-item.component.html",
  styleUrls: ["./action-bar-item.components.scss"]
})
export class ActionBarItemComponent implements OnInit {
  @Input() action: TableActionDefinition | undefined

  @Input() disabled: boolean = true

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
