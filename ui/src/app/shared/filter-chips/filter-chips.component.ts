import {Component, Input, OnInit, Output} from "@angular/core"

@Component({
  selector: "app-filter-chips",
  templateUrl: "./filter-chips.component.html",
  styleUrls: ["./filter-chips.component.scss"]
})
export class FilterChipsComponent implements OnInit {

  @Input()
  name?: string
  @Input()
  chips?: string[]

  constructor() {
  }

  ngOnInit(): void {
  }

  onRemoveChip(chipText: string): void {
    const index = this.chips?.findIndex(i => i === chipText)
    if (index) {
      this.chips?.splice(index, 1)
    }
  }

}
