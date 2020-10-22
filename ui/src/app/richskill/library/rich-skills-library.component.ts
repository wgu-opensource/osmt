import {Component, OnInit} from "@angular/core"
import {RichSkillService} from "../service/rich-skill.service"
import {ApiSkill} from "../ApiSkill"
import {Observable} from "rxjs";
import {ColumnDetails} from "../../table-header/table-header.component";

@Component({
  selector: "app-rich-skills-library",
  templateUrl: "./rich-skills-library.component.html"
})
export class RichSkillsLibraryComponent implements OnInit {

  skills: Observable<ApiSkill[]> | null = null
  loading = true

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

  tableSelectionPerformed(currentlySelected: ApiSkill[]): void {

  }

  filterPerformed(type: string): void {

  }

  headerColumnSortPerformed(columnName: string): void {

  }
}
