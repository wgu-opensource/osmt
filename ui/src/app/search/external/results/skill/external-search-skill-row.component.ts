import {Component, Input, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../../core/SvgHelper"
import {RichSkillService} from "../../../../richskill/service/rich-skill.service"
import {ApiSkillSearchResult} from "../../api/ApiSkillSearchResult"
import {ApiSkillSummary} from "../../../../richskill/ApiSkillSummary"

@Component({
  // tslint:disable-next-line:component-selector
  selector: "[app-external-search-skill-row]",
  templateUrl: "./external-search-skill-row.component.html"
})
export class ExternalSearchSkillRowComponent implements OnInit {

  @Input() searchResult: ApiSkillSearchResult | null = null
  @Input() id = ""
  @Input() nextId = ""

  imported = false

  externalLinkIcon = SvgHelper.path(SvgIcon.EXTERNAL_LINK)
  errorIcon = SvgHelper.path(SvgIcon.ERROR)

  get skill(): ApiSkillSummary | undefined {
    return this.searchResult?.skill
  }

  get isSimilarToLocalSkill(): boolean {
    return !!(this.searchResult?.similarToLocalSkill)
  }

  get importButtonLabel(): string {
    return (this.imported) ? "RSD Imported" : "Import RSD"
  }

  constructor(
    protected richSkillService: RichSkillService
  ) {}

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
    if (this.skill) {
      this.richSkillService.importSkill(
        this.skill.id,
        this.skill.skillName
      ).subscribe(resp => {
        this.imported = true
      })
    }
  }
}
