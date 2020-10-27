import {Component, OnInit} from "@angular/core"
import {ApiSkill} from "../ApiSkill"
import {Observable} from "rxjs"
import {RichSkillService} from "../service/rich-skill.service"
import {CurrentSort} from "../../table/table-header/table-header.component"
import {PublishStatus} from "../../PublishStatus";

@Component({
  selector: "app-rich-skills-library",
  templateUrl: "./rich-skills-library.component.html"
})
export class RichSkillsLibraryComponent implements OnInit {

  skillsLoaded: Observable<ApiSkill[]> | null = null
  skills: ApiSkill[] = []
  loading = true
  selectedSkills: ApiSkill[] = []

  defaultSortDirection = false
  currentSort: CurrentSort | undefined = undefined

  // Total counts (not paginated)
  draftCount = 0
  publishedCount = 0
  archivedCount = 0

  // filters with defaults
  draftApplied = true
  publishedApplied = true
  archivedApplied = false

  totalSkills = this.draftCount + this.publishedCount + this.archivedCount
  selectedFilters: Set<PublishStatus> = new Set([PublishStatus.Unpublished, PublishStatus.Published])

  constructor(
    private richSkillService: RichSkillService
  ) {
  }

  ngOnInit(): void {
    this.getSkills()
  }

  currentlyViewing(): number {
    return (this.draftApplied ? this.draftCount : 0)
      + (this.publishedApplied ? this.publishedCount : 0)
      + (this.archivedApplied ? this.archivedCount : 0)
  }

  getSkills(
    size: number = 50,
    sort: string | undefined = this.formatCurrentSort()
  ): void {
    this.skillsLoaded = this.richSkillService.getSkills(size, sort)
    this.skillsLoaded.subscribe(skills => {
      this.skills = skills
    })
  }

  formatCurrentSort(): string | undefined {
    return this.currentSort ? `${this.currentSort.column}.${this.currentSort.ascending ? "asc" : "desc"}` : undefined
  }

  // Every time a row is toggled, emit the current list of all selected rows

  onRowToggle(selectedSkills: ApiSkill[]): void {
    this.selectedSkills = selectedSkills
  }

  onHeaderColumnSort(sort: CurrentSort): void {
    this.currentSort = sort
    this.getSkills()
  }

  filterControlsChanged(name: string, isChecked: boolean): void {
    switch (name) {
      case "draft": {
        this.draftApplied = isChecked
        break
      }
      case "published": {
        this.publishedApplied = isChecked
        break
      }
      case "archived": {
        this.archivedApplied = isChecked
        break
      }
    }
  }

  handleFiltersChanged(newFilters: Set<PublishStatus>): void {
    this.selectedFilters = newFilters
  }
}
