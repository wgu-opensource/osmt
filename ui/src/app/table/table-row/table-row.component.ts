import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {ApiSkill} from "../../richskill/ApiSkill"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"

export interface SkillWithSelection {
  skill: ApiSkill,
  selected: boolean
}

@Component({
  selector: "app-table-row",
  templateUrl: "./table-row.component.html"
})
export class TableRowComponent implements OnInit {

  @Input() skill: SkillWithSelection | null = null
  @Input() isSelected = false
  @Output() selected: EventEmitter<SkillWithSelection> = new EventEmitter<SkillWithSelection>()


  checkIcon = SvgHelper.path(SvgIcon.CHECK)
  moreIcon = SvgHelper.path(SvgIcon.MORE)

  constructor() { }

  ngOnInit(): void {
  }

  onSelect(event: MouseEvent): void {
    this.isSelected = !this.isSelected

    if (!this.skill) {
      throw Error() // stub for now
    } else {
      this.selected.emit(this.skill)
    }
  }

}
