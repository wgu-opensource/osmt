import {Component, Input, OnInit} from "@angular/core";
import {TableActionDefinition} from "./table-action-bar.component";

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
    const cb = this.action?.callback
    if (cb !== undefined && this.action !== undefined) {
      cb(this.action)
    }
    return false
  }
}
