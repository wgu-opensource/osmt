import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {FormControl} from "@angular/forms"

@Component({
  selector: "app-size-pagination",
  templateUrl: "./size-pagination.component.html",
  styleUrls: ["./size-pagination.component.scss"]
})
export class SizePaginationComponent implements OnInit {

  @Input()
  values: number[] = []
  @Output()
  changeValue: EventEmitter<number> = new EventEmitter()
  control: FormControl = new FormControl(undefined)
  @Input()
  currentSize?: number

  constructor() {
  }

  ngOnInit(): void {
  }

  onValueChange(value: number): void {
    this.control.patchValue(value)
    this.changeValue.emit(value)
  }

}
