import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../../core/SvgHelper"
import {ApiSkillSummary} from "../../../../richskill/ApiSkillSummary"

@Component({
  // tslint:disable-next-line:component-selector
  selector: "[app-external-search-skill-row]",
  templateUrl: "./external-search-skill-row.component.html"
})
export class ExternalSearchSkillRowComponent implements OnInit {

  @Input() skill: ApiSkillSummary | null = null
  @Input() id = ""
  @Input() nextId = ""

  externalLinkIcon = SvgHelper.path(SvgIcon.EXTERNAL_LINK)

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
    return (this.skill?.occupations?.filter(o => !!o.detailed).map(o => o.detailed) ?? []).join("; ")
  }

  handleClickNext(): boolean {
    if (!this.nextId) { return false }
    const target = document.getElementById(this.nextId) as HTMLElement
    if (!target) { return false }

    target.focus()
    target.scrollIntoView()
    return false
  }

  onImportSkillClicked(): void {
    // TODO: Call Import.
  }
}
