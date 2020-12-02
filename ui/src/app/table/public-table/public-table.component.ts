import {Component} from "@angular/core"
import {PublishStatus} from "../../PublishStatus"
import {ApiSkillSummary} from "../../richskill/ApiSkillSummary"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {AbstractTableComponent} from "../abstract-table.component"

@Component({
  selector: "app-public-table",
  templateUrl: "./public-table.component.html"
})
export class PublicTableComponent extends AbstractTableComponent<ApiSkillSummary> {

  archiveIcon = SvgHelper.path(SvgIcon.ARCHIVE)
  constructor() {
    super()
  }

  isArchived(skill: ApiSkillSummary): boolean {
    if (skill) {
      return skill.status === PublishStatus.Archived
    }
    return false
  }

}
