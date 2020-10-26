import {Component, OnInit} from "@angular/core"
import {ApiSkill} from "../ApiSkill"
import {Observable} from "rxjs"
import {ApiSearch, RichSkillSearchService} from "../service/rich-skill-search.service"
import {RichSkillService} from "../service/rich-skill.service";

@Component({
  selector: "app-rich-skills-library",
  templateUrl: "./rich-skills-library.component.html"
})
export class RichSkillsLibraryComponent implements OnInit {

  skillsLoaded: Observable<ApiSkill[]> | null = null
  loading = true
  selectedSkills: ApiSkill[] = []

  private selectAllTemplate = "Select All (%s)"
  tableColumns: string[] = ["Category", "Skill", "Select All ()"]
  private skills: ApiSkill[] | undefined;

  constructor(
    private richSkillSearchService: RichSkillSearchService,
    private richSkillService: RichSkillService
  ) {
  }

  ngOnInit(): void {
    this.getSkills()
  }

  getSkills(
    size: number = 50,
    from: number = 0,
    status: string[] = [],
    sort: string = "category.asc",
  ): void {
    const searchBody: Partial<ApiSearch> = {}
    console.log(searchBody.query)
    this.skillsLoaded = this.richSkillService.getSkills()
    this.skillsLoaded.subscribe(skills => {
      this.skills = skills
    })
    // this.skills = this.richSkillSearchService.searchSkills(size, from, status, sort, searchBody)
  }

  filterPerformed(type: string): void {

  }

  // Every time a row is toggled, emit the current list of all selected rows
  onRowToggle(selectedSkills: ApiSkill[]): void {
    this.selectedSkills = selectedSkills
  }

  // propogate the header component's column sort event upward
  headerColumnSortPerformed(columnName: string): void {
  }

  filterControlsChanged(name: string, isChecked: boolean): void {

  }
}
