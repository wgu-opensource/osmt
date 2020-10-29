import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {OccupationsFormatter} from "../../job-codes/Jobcode"
import {IApiSkillSummary} from "../../richskill/ApiSkillSummary"
import {PublishStatus} from "../../PublishStatus"

export interface SkillWithMetadata {
  skill: IApiSkillSummary,
  selected: boolean
}

@Component({
  selector: "app-table-row",
  templateUrl: "./table-row.component.html"
})
export class TableRowComponent implements OnInit {

  @Input() skill: IApiSkillSummary | null = null
  @Input() isSelected = false
  @Input() id = ""

  @Output() rowSelected = new EventEmitter<IApiSkillSummary>()


  upIcon = SvgHelper.path(SvgIcon.ICON_UP)
  checkIcon = SvgHelper.path(SvgIcon.CHECK)

  constructor() { }

  ngOnInit(): void {
    if (!this.id) {
      throw Error()
    }
  }

  getFormattedKeywords(): string {
    return this.skill?.keywords?.join("; ") ?? ""
  }

  getFormattedOccupations(): string {
    return new OccupationsFormatter(this.skill?.occupations ?? []).detailedGroups()
  }

  selected(): void {
    if (!this.skill) {
      throw Error() // stub for now
    } else {
      this.rowSelected.emit(this.skill)
    }
  }

  isStatus(status: PublishStatus): boolean {
    if (this.skill) {
      return this.skill.status === status
    }
    return false
  }

  isPublished(): boolean {
    return this.isStatus(PublishStatus.Published) || this.isArchived()
  }

  isArchived(): boolean {
    return this.isStatus(PublishStatus.Archived)
  }
}
