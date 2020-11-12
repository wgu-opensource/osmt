import {Component} from "@angular/core"
import {TableComponent} from "../skills-library-table/table.component"
import {PublishStatus} from "../../PublishStatus"
import {IApiSkillSummary} from "../../richskill/ApiSkillSummary"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper";

@Component({
  selector: "app-public-table",
  templateUrl: "./public-table.component.html"
})
export class PublicTableComponent extends TableComponent {

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
