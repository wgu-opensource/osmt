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
  keywords?: string[]

  constructor() {
  }

  ngOnInit(): void {
  }

  onRemoveChip(chipText: string): void {
    const index = this.keywords?.findIndex(i => i === chipText) ?? 0
    if (index >= 0) {
      this.keywords?.splice(index, 1)
    }
  }

}
