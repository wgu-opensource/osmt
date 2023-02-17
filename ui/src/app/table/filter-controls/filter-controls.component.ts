import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {PublishStatus} from "../../PublishStatus";
import {FilterDropdown} from "../../models/filter-dropdown.model"

@Component({
  selector: "app-filter-controls",
  templateUrl: "./filter-controls.component.html",
  styleUrls: ["./filter-controls.component.scss"]
})
export class FilterControlsComponent implements OnInit {
  @Input() selectedFilters: Set<PublishStatus> = new Set()
  @Output() filtersChanged: EventEmitter<Set<PublishStatus>> = new EventEmitter<Set<PublishStatus>>()
  chipsValues: FilterDropdown = {
    categories: [],
    certifications: [],
    employers: [],
    alignments: [],
    keywords: [],
    occupations: [],
    standards: []
  }

  constructor() {
  }

  ngOnInit(): void {
  }

  onFilterChange(status: PublishStatus, isChecked: boolean): void {
    if (isChecked) {
      this.selectedFilters.add(status)
    } else {
      this.selectedFilters.delete(status)
    }
    this.filtersChanged.emit(this.selectedFilters)
  }
  onFilterChangeDraft(isChecked: boolean): void {
    return this.onFilterChange(PublishStatus.Draft, isChecked)
  }
  onFilterChangePublished(isChecked: boolean): void {
    return this.onFilterChange(PublishStatus.Published, isChecked)
  }
  onFilterChangeArchived(isChecked: boolean): void {
    return this.onFilterChange(PublishStatus.Archived, isChecked)
  }

  get isDraftChecked(): boolean | undefined { return this.isStatusChecked(PublishStatus.Draft) }
  get isPublishedChecked(): boolean | undefined { return this.isStatusChecked(PublishStatus.Published) }
  get isArchivedChecked(): boolean | undefined { return this.isStatusChecked(PublishStatus.Archived) }
  isStatusChecked(status: PublishStatus): boolean | undefined {
    return this.selectedFilters.has(status) ? true : undefined
  }
}
