import {Component, EventEmitter, Input, Output} from "@angular/core"
import {AbstractPillControl} from "../pill-control";

@Component({
  selector: "app-pill-group",
  templateUrl: "./pill-group.component.html"
})
export class PillGroupComponent<TValue extends AbstractPillControl>{

  @Input() pillControls: TValue[] = []
  @Output() pillClicked: EventEmitter<TValue> = new EventEmitter()

  get hasPillClickedObservers(): boolean {
    return this.pillClicked.observers.length > 0
  }

  onPillClicked(pill: TValue) {
    this.pillClicked.emit(pill)
  }
}
