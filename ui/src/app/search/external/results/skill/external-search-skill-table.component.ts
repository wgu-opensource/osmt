import {Component} from "@angular/core"
import {ApiSkillSummary} from "../../../../richskill/ApiSkillSummary"
import {AbstractTableComponent} from "../../../../table/abstract-table.component"

@Component({
  selector: "app-external-search-skill-table",
  templateUrl: "./external-search-skill-table.component.html"
})
export class ExternalSearchSkillTableComponent extends AbstractTableComponent<ApiSkillSummary> {

  constructor() {
    super()
  }

}
