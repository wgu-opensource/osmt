import {Component, Input} from "@angular/core"
import {Router} from "@angular/router";
import {TableActionDefinition} from "../table/skills-library-table/has-action-definitions";

@Component({
  selector: "app-vertical-action-bar",
  templateUrl: "./vertical-action-bar.component.html"
})
export class VerticalActionBarComponent {
  @Input() actions: TableActionDefinition[] = []
  @Input() data?: any

  constructor(
    protected router: Router,
  ) {
  }

  get primaryActions(): TableActionDefinition[] {
    return this.actions.filter(it => it.primary)
  }

  get secondaryActions(): TableActionDefinition[] {
    return this.actions.filter(it => !it.primary)
  }
}
