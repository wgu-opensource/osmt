import {Component, EventEmitter, Input, Output} from "@angular/core"
import {AbstractPillControl} from "./pill-control";

@Component({
  selector: "app-pill",
  templateUrl: "./pill.component.html"
})
export class PillComponent<TValue extends AbstractPillControl>{

  @Input() tabSelectEnabled: boolean = true
  @Input() control?: TValue = undefined
  @Output() clicked: EventEmitter<TValue> = new EventEmitter()

  get isSelected(): boolean {
    return !!this.control?.isSelected
  }

  get hasClickedObservers(): boolean {
    return this.clicked.observers.length > 0
  }

  get hasSecondaryLabel(): boolean {
    return !!this.secondaryLabel
  }

  get primaryLabel(): string {
    return (this.control) ? this.control.primaryLabel : ''
  }

  get secondaryLabel(): string | undefined {
    return (this.control) ? this.control.secondaryLabel : undefined
  }

  onClick(): void {
    this.clicked.emit(this.control)
  }
}
