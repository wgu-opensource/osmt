import {Component, Input} from "@angular/core"
import {FormControl} from "@angular/forms"
import {MetadataType} from "../rsd-metadata.enum"
import {Meta} from "@angular/platform-browser"

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

  protected readonly MetadataType = MetadataType
  @Input()
  isVisible: () => boolean = () => false

  onValueChange(value: string): void {
    this.control?.patchValue(value)
    this.currentSelection = value
    console.log(this.control?.value)
  }
}
