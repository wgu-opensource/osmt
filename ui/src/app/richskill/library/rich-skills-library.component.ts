import {Component, OnInit} from "@angular/core"
import {ApiSkill, SkillsWithCount} from "../ApiSkill"
import {Observable} from "rxjs"
import {RichSkillService} from "../service/rich-skill.service"
import {CurrentSort} from "../../table/table-header/table-header.component"
import {PublishStatus} from "../../PublishStatus"

@Component({
  selector: "app-rich-skills-library",
  templateUrl: "./rich-skills-library.component.html"
})
export class RichSkillsLibraryComponent implements OnInit {

  skillsLoaded: Observable<SkillsWithCount> | null = null
  skills: ApiSkill[] = []
  loading = true
  selectedSkills: ApiSkill[] = []

  defaultSortDirection = false
  currentSort: CurrentSort | undefined = undefined

  // filters with defaults
  draftApplied = true
  publishedApplied = true
  archivedApplied = false

  totalSkills = 0
  selectedFilters: Set<PublishStatus> = new Set([PublishStatus.Unpublished, PublishStatus.Published])

  constructor(
    private richSkillService: RichSkillService
  ) {
  }

  ngOnInit(): void {
    this.getSkills()
  }

  getSkills(
    size: number = 50,
    sort: string | undefined = this.formatCurrentSort()
  ): void {
    this.skillsLoaded = this.richSkillService.getSkills(size, sort)
    this.skillsLoaded.subscribe(({skills, total}) => {
      this.skills = skills
      this.totalSkills = total
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
