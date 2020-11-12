import {Component, Input} from "@angular/core";

interface IActionDefinition {
  label?: string
  icon?: string
  primary?: boolean
  offset?: boolean
  callback?: (actionDefinition: TableActionDefinition, data?: any) => void
  visible?: (data?: any) => boolean
}

export class TableActionDefinition {
  label: string = "Button"
  icon: string = "dismiss"
  primary: boolean = false
  offset: boolean = false
  callback?: ((actionDefinition: TableActionDefinition, data?: any) => void)
  visible?: (data?: any) => boolean

  constructor({label, icon, primary, offset, callback, visible}: IActionDefinition) {
    this.label = label ?? ""
    this.icon = icon ?? ""
    this.callback = callback
    this.visible = visible
    this.primary = primary ?? false
    this.offset = offset ?? false
  }

  fire(data?: any): void {
    if (this.callback !== undefined) {
      this.callback(this, data)
    }
  }

  get isVisible(): boolean {
    if (this.visible !== undefined) {
      return this.visible()
    }
    return true
  }
}

@Component({
  template: ""
})
export class HasActionDefinitions {
  @Input() actions: TableActionDefinition[] = []

  constructor() {
  }

  visibleActions(data?: any): TableActionDefinition[] {
    return this.actions.filter((def: TableActionDefinition) => {
      if (def.visible !== undefined) {
        return def.visible(data)
      }
      return true
    })
  }

  clickAction(action?: TableActionDefinition, data?: any): boolean {
    action?.fire(data)
    return false
  }
}
