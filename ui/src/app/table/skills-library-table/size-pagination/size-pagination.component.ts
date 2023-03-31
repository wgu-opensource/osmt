import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {FormControl} from "@angular/forms"

@Component({
  selector: "app-size-pagination",
  templateUrl: "./size-pagination.component.html",
  styleUrls: ["./size-pagination.component.scss"]
})
export class SizePaginationComponent implements OnInit {

  values: number[] = [50, 100, 150]
  @Output()
  changeValue: EventEmitter<number> = new EventEmitter()
  control: FormControl = new FormControl(undefined)
  @Input()
  currentSize = 50

  constructor() {
  }

  ngOnInit(): void {
  }

  onValueChange(value: number): void {
    this.control.patchValue(value)
    this.changeValue.emit(value)
  }

}
