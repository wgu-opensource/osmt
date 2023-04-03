import {Component, Input} from "@angular/core"
import {FormControl} from "@angular/forms"

@Component({
  selector: "app-size-pagination",
  templateUrl: "./size-pagination.component.html",
  styleUrls: ["./size-pagination.component.scss"]
})
export class SizePaginationComponent {

  readonly values: number[] = [50, 100, 150]
  @Input()
  control?: FormControl

  onValueChange(value: number): void {
    this.control?.patchValue(value)
  }

}
