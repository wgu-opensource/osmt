import {Component} from "@angular/core"
import {SkillTableComponent} from "../skills-library-table/skill-table.component"
import {PublishStatus} from "../../PublishStatus"
import {IApiSkillSummary} from "../../richskill/ApiSkillSummary"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper";
import {AbstractTableComponent} from "../abstract-table.component";

@Component({
  selector: "app-public-table",
  templateUrl: "./public-table.component.html"
})
export class PublicTableComponent extends AbstractTableComponent<IApiSkillSummary> {

  archiveIcon = SvgHelper.path(SvgIcon.ARCHIVE)
  constructor() {
    super()
  }

  isArchived(skill: IApiSkillSummary): boolean {
    if (skill) {
      return skill.status === PublishStatus.Archived
    }
    return false
  }

}
