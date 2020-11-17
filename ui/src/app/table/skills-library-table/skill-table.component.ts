import {Component} from "@angular/core"
import {ApiSkillSummary} from "../../richskill/ApiSkillSummary"
import {AbstractTableComponent} from "../abstract-table.component"

@Component({
  selector: "app-skill-table",
  templateUrl: "./skill-table.component.html"
})
export class SkillTableComponent extends AbstractTableComponent<ApiSkillSummary> {

  constructor() {
    super()
  }

}
