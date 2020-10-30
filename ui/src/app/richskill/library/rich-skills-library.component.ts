import {Component, OnInit} from "@angular/core"
import {ApiSkill, ApiSkillSortOrder} from "../ApiSkill"
import {Observable} from "rxjs"
import {RichSkillService} from "../service/rich-skill.service"
import {PublishStatus} from "../../PublishStatus"
import {PaginatedSkills} from "../service/rich-skill-search.service"
import {IApiSkillSummary} from "../ApiSkillSummary"

@Component({
  selector: "app-rich-skills-library",
  templateUrl: "./rich-skills-library.component.html"
})
export class RichSkillsLibraryComponent implements OnInit {

  skillsLoaded: Observable<PaginatedSkills> | null = null
  skills: IApiSkillSummary[] = []
  selectedSkills: IApiSkillSummary[] = []
  loading = true

  defaultSortDirection = false
  currentSort: ApiSkillSortOrder | undefined = undefined

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
    sort: ApiSkillSortOrder | undefined = this.currentSort
  ): void {
    this.skillsLoaded = this.richSkillService.getSkills(size, [...this.selectedFilters], sort)
    this.skillsLoaded.subscribe(({skills, totalCount}) => {
      this.skills = skills
      this.totalSkills = totalCount
    })
  }

  handleHeaderColumnSort(sort: ApiSkillSortOrder): void {
    console.log("library got sort: " + sort)
    this.currentSort = sort
    this.getSkills()
  }

  filterControlsChanged(name: string, isChecked: boolean): void {
    this.selectedSkills = []
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
    this.getSkills()
  }

  handleSelectedRows(uuids: IApiSkillSummary[]): void {
    this.selectedSkills = uuids
  }

  handleSelectAll(isChecked: boolean): void {
    console.log(`Select all ${isChecked ? "checked" : "unchecked"}`)
  }
}
