import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from "@angular/core"
import {PublishStatus} from "../../PublishStatus";
import {FilterDropdown} from "../../models/filter-dropdown.model"
import {FormBuilder, FormGroup} from "@angular/forms"

@Component({
  selector: "app-filter-controls",
  templateUrl: "./filter-controls.component.html",
  styleUrls: ["./filter-controls.component.scss"]
})
export class FilterControlsComponent implements OnInit, OnChanges {
  @Input() selectedFilters: Set<PublishStatus> = new Set()
  @Output() keywordsChanged: EventEmitter<FilterDropdown> = new EventEmitter<FilterDropdown>()
  @Output() filtersChanged: EventEmitter<Set<PublishStatus>> = new EventEmitter<Set<PublishStatus>>()
  @Input()
  keywords?: FilterDropdown
  filterFg: FormGroup

  constructor(
    protected formBuilder: FormBuilder
  ) {
    this.filterFg = this.configureFilterFg()
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.filterFg.patchValue(this.keywords ?? {})
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

  applyFilter(event: FilterDropdown): void {
    this.keywords = event
    this.keywordsChanged.emit(this.keywords)
  }

  private configureFilterFg(): FormGroup {
    return this.formBuilder.group({
      categories: [],
      keywords: [],
      standards: [],
      alignments: [],
      certifications: [],
      occupations: [],
      employers: [],
      authors: []
    })
  }

}
