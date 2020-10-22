import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {ColumnDetails} from "../table-header.component";

@Component({
  selector: "app-label-select-small",
  templateUrl: "./label-select-small.component.html"
})
export class LabelSelectSmallComponent implements OnInit {

  @Input() columns: string[] = ["one"]
  @Output() labelClicked: EventEmitter<string> = new EventEmitter<string>()

  constructor() { }

  ngOnInit(): void {
    console.log(this.columns)
  }

  emitClick(name: string): void  {
    this.labelClicked.emit(name)
  }

}
