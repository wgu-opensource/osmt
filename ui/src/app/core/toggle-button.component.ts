import {Component, Input, Output, EventEmitter} from "@angular/core"
import {SvgIcon, SvgHelper} from "./SvgHelper"
import {Observable} from "rxjs"

export enum ToggleButtonOption {
  Option1,
  Option2
}

@Component({
  selector: "app-toggle-button",
  templateUrl: "./toggle-button.component.html"
})
export class ToggleButtonComponent {
  readonly SvgHelper = SvgHelper
  readonly ToggleButtonOption: typeof ToggleButtonOption = ToggleButtonOption

  @Input() selectedOption: ToggleButtonOption|null = null

  @Input() option1Label: string|null = null
  @Input() option1Icon: SvgIcon|null = null
  @Input() option1Loading = false

  @Input() option2Label: string|null = null
  @Input() option2Icon: SvgIcon|null = null
  @Input() option2Loading = false

  @Output() optionClick = new EventEmitter<ToggleButtonOption>()

  getOptionIconPath(option: ToggleButtonOption): string|null {
    switch (option) {
      case ToggleButtonOption.Option1:
        return (this.option1Icon) ? SvgHelper.path(this.option1Icon) : null
      case ToggleButtonOption.Option2:
        return (this.option2Icon) ? SvgHelper.path(this.option2Icon) : null
      default:
        return null
    }
  }

  isOptionSelected(option: ToggleButtonOption): boolean {
    return (this.selectedOption === option)
  }

  isOptionLoading(option: ToggleButtonOption): boolean {
    switch (option) {
      case ToggleButtonOption.Option1:
        return (this.option1Loading === true)
      case ToggleButtonOption.Option2:
        return (this.option2Loading === true)
      default:
        return false
    }
  }

  handleOptionClicked(option: ToggleButtonOption): void {
    if (option !== undefined && option != null) {
      this.optionClick.emit(option)
    }
  }
}
