import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"

@Component({
  selector: "app-filter-controls",
  templateUrl: "./filter-controls.component.html"
})
export class FilterControlsComponent implements OnInit {

  @Input() draftSelected = false
  @Input() publishSelected = false
  @Input() archivedSelected = false

  @Output() draftFilterEmitter: EventEmitter<boolean> = new EventEmitter<boolean>()
  @Output() publishedFilterEmitter: EventEmitter<boolean> = new EventEmitter<boolean>()
  @Output() archivedFilterEmitter: EventEmitter<boolean> = new EventEmitter<boolean>()

  constructor() { }

  ngOnInit(): void {
  }

  onFilterChange(filterName: string, isChecked: boolean): void {

    switch (filterName.toLowerCase()) {
      case "draft": {
        this.draftFilterEmitter.emit(isChecked)
        break
      }
      case "published": {
        this.publishedFilterEmitter.emit(isChecked)
        break
      }
      case "archived": {
        this.archivedFilterEmitter.emit(isChecked)
        break
      }
    }
  }
}
