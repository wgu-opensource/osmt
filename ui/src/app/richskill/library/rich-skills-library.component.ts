import {Component, OnInit} from "@angular/core"
import {ApiSkill, ApiSkillSortOrder} from "../ApiSkill"
import {Observable} from "rxjs"
import {RichSkillService} from "../service/rich-skill.service"
import {PublishStatus} from "../../PublishStatus"
import {PaginatedSkills} from "../service/rich-skill-search.service"
import {IApiSkillSummary} from "../ApiSkillSummary"
import {SkillActions} from "../../table/table-row/ellipses-menu/ellipses-menu.component"

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
    this.skillsLoaded = this.richSkillService.getSkills(size, sort)
    this.skillsLoaded.subscribe(({skills, totalCount}) => {
      this.skills = skills
      this.totalSkills = totalCount
    })
  }

  getActionsForOneSkill(): SkillActions {
    return {
      handleAddToCollection: skill => {
        // Do something then load skills
        console.log("added to collection!")
      },
      handleUnarchive: skill => {
        console.log("Unarchived!")

      },
      handleArchive: skill => {
        console.log("Archived!")

      },
      handlePublish: skill => {
        console.log("Published!")
      }
    }
  }

  onHeaderColumnSort(sort: ApiSkillSortOrder): void {
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
  }

  rowSelected(uuids: IApiSkillSummary[]): void {
    this.selectedSkills = uuids
  }
}
