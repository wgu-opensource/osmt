import {Component, OnInit, Output, EventEmitter} from "@angular/core"

@Component({
  selector: "app-create-collection-action-bar",
  template: ""
})
export class AbstractCreateCollectionActionbarComponent implements OnInit {

  @Output() saveClicked = new EventEmitter<void>()
  @Output() cancelClicked = new EventEmitter<void>()

  constructor() { }

  ngOnInit(): void {
  }

  handleSave(): void {
    this.saveClicked.emit()
  }

  handleCancel(): void {
    this.cancelClicked.emit()
  }
}
