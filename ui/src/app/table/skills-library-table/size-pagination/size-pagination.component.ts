import {Component, Input} from "@angular/core"
import {FormControl} from "@angular/forms"

export const minimumThreshold = 50

@Component({
  selector: "app-size-pagination",
  templateUrl: "./size-pagination.component.html",
  styleUrls: ["./size-pagination.component.scss"]
})
export class SizePaginationComponent {

  readonly values: number[] = [50, 100, 150]
  @Input()
  control?: FormControl
  @Input()
  currentSize = 50
  @Input()
  isVisible: () => boolean = () => false

  onValueChange(value: number): void {
    this.control?.patchValue(value)
  }

}
