import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {ApiSkillSummary} from "../ApiSkillSummary"
import {PublishStatus} from "../../PublishStatus"
import {TableActionDefinition} from "../../table/skills-library-table/has-action-definitions";

@Component({
  // tslint:disable-next-line:component-selector
  selector: "[app-skill-list-row]",
  templateUrl: "./skill-list-row.component.html"
})
export class SkillListRowComponent implements OnInit {

  @Input() skill: ApiSkillSummary | null = null
  @Input() isSelected = false
  @Input() id = ""
  @Input() nextId = ""
  @Input() rowActions: TableActionDefinition[] = []

  @Output() rowSelected = new EventEmitter<ApiSkillSummary>()
  @Output() focusActionBar = new EventEmitter<void>()

  upIcon = SvgHelper.path(SvgIcon.ICON_UP)
  checkIcon = SvgHelper.path(SvgIcon.CHECK)
  moreIcon = SvgHelper.path(SvgIcon.MORE)

  constructor() { }

  ngOnInit(): void {
    if (!this.id) {
      throw Error()
    }
  }

  getFormattedCategories(): string {
    return this.skill?.categories?.join("; ") ?? ""
  }

  getFormattedKeywords(): string {
    return this.skill?.keywords?.join("; ") ?? ""
  }

  getFormattedOccupations(): string {
    return (this.skill?.occupations?.filter(o => !!o.detailed).map(o => o.detailed) ?? []).join("; ")
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
    return this.isStatus(PublishStatus.Published)
  }

  isArchived(): boolean {
    return this.isStatus(PublishStatus.Archived)
  }

  handleClickNext(): boolean {
    if (!this.nextId) { return false }
    const target = document.getElementById(this.nextId) as HTMLElement
    if (!target) { return false }

    target.focus()
    target.scrollIntoView()
    return false
  }
}
