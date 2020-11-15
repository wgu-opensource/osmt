import {Component, EventEmitter, Input, Output} from "@angular/core"
import {IApiSkillSummary} from "../../richskill/ApiSkillSummary"
import {AbstractTableComponent} from "../abstract-table.component"

@Component({
  selector: "app-skill-table",
  templateUrl: "./skill-table.component.html"
})
export class SkillTableComponent extends AbstractTableComponent<IApiSkillSummary> {

  constructor() {
    super()
  }

}
