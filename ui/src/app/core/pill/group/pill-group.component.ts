import {Component, EventEmitter, Input, Output} from "@angular/core"
import {AbstractPillControl} from "../pill-control";

@Component({
  selector: "app-pill-group",
  templateUrl: "./pill-group.component.html"
})
export class PillGroupComponent<TValue extends AbstractPillControl>{

  @Input() pillControls: TValue[] = []
  @Output() pillClicked: EventEmitter<TValue> = new EventEmitter()
  collapsed = true;

  get hasPillClickedObservers(): boolean {
    return this.pillClicked.observers.length > 0
  }

  toggleCollapse(event: Event): void {
    this.collapsed = !this.collapsed
    event.preventDefault();
    event.stopPropagation();
  }
  
  expand(): void {
    this.collapsed = false
  }

  onPillClicked(pill: TValue): void {
    this.pillClicked.emit(pill)
  }
}
