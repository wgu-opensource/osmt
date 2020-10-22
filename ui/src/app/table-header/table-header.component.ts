import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"

export interface ColumnDetails {
  name: string
  onFilter: (name: string) => void
}

@Component({
  selector: "app-table-header",
  templateUrl: "./table-header.component.html"
})
export class TableHeaderComponent implements OnInit {

  @Input() columns: string[] = []
  @Output() columnClicked: EventEmitter<string> = new EventEmitter<string>()

  constructor() { }

  ngOnInit(): void {
  }

  emitColumnClick(name: string): void {
    this.columnClicked.emit(name)
  }
}
