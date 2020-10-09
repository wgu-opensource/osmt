import {Component, Input, OnInit} from "@angular/core";
import {FormControl} from "@angular/forms";

@Component({
  selector: "app-skill-collections-display",
  templateUrl: "./skill-collections-display.component.html"
})
export class SkillCollectionsDisplayComponent implements OnInit {

  @Input() control: FormControl = new FormControl()
  @Input() label: string = ""
  @Input() placeholder: string = ""
  @Input() errorMessage: string = ""
  @Input() helpMessage: string = ""
  @Input() required: boolean = false
  @Input() name: string = ""

  toRemove: string[] = []
  allCollections: string[] = []

  constructor() {
  }

  ngOnInit(): void {
    this.allCollections = this.control.value.slice()
  }

  handleClickUndo(collection: string): boolean {
    const idx = this.toRemove.indexOf(collection)
    if (idx !== -1) {
      this.toRemove.splice(idx, 1)
      this.control.setValue(this.control.value.concat(collection))

      if (this.toRemove.length < 1) {
        this.control.markAsPristine()
        this.control.markAsUntouched()
      }
    }

    return false
  }

  handleClickRemove(collection: string): boolean {
    const copy: string[] = this.control.value
    const idx = copy.indexOf(collection)
    this.toRemove.push(collection)
    if (idx !== -1) {
      copy.splice(idx, 1) // remove element from copy
      this.control.markAsDirty()
      this.control.markAsTouched()
      this.control.setValue(copy)
    }

    return false
  }
}
