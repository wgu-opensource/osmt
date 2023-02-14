import {Component, Input, OnInit} from "@angular/core"

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
    this.chips = this.chips?.filter(i => i !== chipText)
  }

}
