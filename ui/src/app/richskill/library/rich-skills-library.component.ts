import {Component, OnInit} from "@angular/core"
import {RichSkillService} from "../service/rich-skill.service"
import {ApiSkill} from "../ApiSkill"
import {Observable} from "rxjs"
import {ColumnDetails} from "../../table/table-header/table-header.component";
import {SkillWithSelection} from "../../table/table-row/table-row.component";

@Component({
  selector: "app-rich-skills-library",
  templateUrl: "./rich-skills-library.component.html"
})
export class RichSkillsLibraryComponent implements OnInit {

  skills: Observable<ApiSkill[]> | null = null
  loading = true
  selectedSkills: ApiSkill[] = []

  private selectAllTemplate = "Select All (%s)"
  tableColumns: string[] = ["Category", "Skill", "Select All ()"]

  constructor(private richSkillService: RichSkillService) {
  }

  ngOnInit(): void {
    this.getSkills()
  }

  getSkills(): void {
    this.skills = this.richSkillService.getSkills()
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
}
