import {Component, Input, OnInit} from "@angular/core";


interface IActionDefinition {
  label?: string
  icon?: string
  primary?: boolean
  offset?: boolean
  callback?: (actionDefinition: TableActionDefinition) => void
  visible?: () => boolean
}

export class TableActionDefinition {
  label: string = "Button"
  icon: string = "dismiss"
  primary: boolean = false
  offset: boolean = false
  callback?: ((actionDefinition: TableActionDefinition) => void)
  visible?: (() => void)

  constructor({label, icon, primary, offset, callback, visible}: IActionDefinition) {
    this.label = label ?? ""
    this.icon = icon ?? ""
    this.callback = callback
    this.visible = visible
    this.primary = primary ?? false
    this.offset = offset ?? false
  }
}

@Component({
  selector: "app-table-action-bar",
  templateUrl: "./table-action-bar.component.html"
})
export class TableActionBarComponent implements OnInit {
  @Input() actions: TableActionDefinition[] = []

  constructor() {
  }

  ngOnInit(): void {
  }

  visibleActions(): TableActionDefinition[] {
    return this.actions.filter((def: TableActionDefinition) => {
      if (def.visible !== undefined) {
        return def.visible()
      }
      return true
    })
  }
}
