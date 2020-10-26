import {Component, OnInit} from "@angular/core"
import {ApiSkill} from "../ApiSkill"
import {Observable} from "rxjs"
import {RichSkillService} from "../service/rich-skill.service"
import {CurrentSort} from "../../table/table-header/table-header.component"

@Component({
  selector: "app-rich-skills-library",
  templateUrl: "./rich-skills-library.component.html"
})
export class RichSkillsLibraryComponent implements OnInit {

  skills: Observable<ApiSkill[]> | null = null
  loading = true
  selectedSkills: ApiSkill[] = []

  defaultSortDirection = false
  currentSort: CurrentSort | undefined = undefined

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
    this.skills = this.richSkillService.getSkills(size, sort)
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

  }
}
