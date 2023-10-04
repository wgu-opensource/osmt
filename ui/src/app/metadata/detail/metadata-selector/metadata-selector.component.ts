import { Component, Input } from "@angular/core"
import { FormControl } from "@angular/forms"
import { MetadataType } from "../../rsd-metadata.enum"

@Component({
  selector: "app-metadata-selector",
  templateUrl: "./metadata-selector.component.html",
  styleUrls: ["./metadata-selector.component.scss"]
})
export class MetadataSelectorComponent {

  @Input()
  control?: FormControl
  @Input()
  currentSelection?: string

  types: string[] = []
  @Input()
  isVisible: () => boolean = () => false

  constructor() {
    this.types = Object.values(MetadataType).sort((a, b) => {
      if(a < b) { return -1; }
      if(a > b) { return 1; }
      return 0;
    })
  }

  onValueChange(value: string): void {
    this.control?.patchValue(value)
    this.currentSelection = value
  }
}
