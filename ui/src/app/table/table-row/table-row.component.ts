import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {ApiSkill} from "../../richskill/ApiSkill"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {OccupationsFormatter} from "../../job-codes/Jobcode";
import {TableActionDefinition} from "../has-action-definitions";

export interface SkillWithSelection {
  skill: ApiSkill,
  selected: boolean
}

@Component({
  selector: "app-table-row",
  templateUrl: "./table-row.component.html"
})
export class TableRowComponent implements OnInit {

  @Input() skill: ApiSkill | null = null
  @Input() isSelected = false
  @Output() selected: EventEmitter<ApiSkill> = new EventEmitter<ApiSkill>()
  @Input() id = "table-row"


  checkIcon = SvgHelper.path(SvgIcon.CHECK)
  moreIcon = SvgHelper.path(SvgIcon.MORE)

  constructor() { }

  ngOnInit(): void {
  }

  getFormattedKeywords(): string {
    return this.skill?.keywords?.join("; ") ?? ""
  }

  getFormattedOccupations(): string {
    return new OccupationsFormatter(this.skill?.occupations ?? []).detailedGroups()
  }

  onSelect(): void {
    if (!this.skill) {
      throw Error() // stub for now
    } else {
      this.selected.emit(this.skill)
    }
  }

  getRowActions(): TableActionDefinition[] {
    return [
      new TableActionDefinition({
        label: "First",
        callback: () => { console.log("clicked first") }
      }),
      new TableActionDefinition({
        label: "Second",
        callback: () => { console.log("clicked second") }
      })
    ]
  }
}
