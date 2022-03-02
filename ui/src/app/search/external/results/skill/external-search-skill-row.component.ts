import {Component, HostBinding, Input, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../../core/SvgHelper"
import {RichSkillService} from "../../../../richskill/service/rich-skill.service"
import {ApiSkillSearchResult} from "../../api/ApiSkillSearchResult"
import {ApiSkillSummary} from "../../../../richskill/ApiSkillSummary"
import {ToastService} from "../../../../toast/toast.service"

@Component({
  // tslint:disable-next-line:component-selector
  selector: "[app-external-search-skill-row]",
  templateUrl: "./external-search-skill-row.component.html"
})
export class ExternalSearchSkillRowComponent implements OnInit {

  static IMPORT_SUCCESS_TITLE = "Success!"
  static IMPORT_SUCCESS_MSG = "You added 1 RSD to the library."
  static IMPORT_CONFIRMATION_MSG =
    "Skill statement is very similar to one already in the library.\n\n" +
    "OK to import?"

  @Input() searchResult: ApiSkillSearchResult | null = null
  @Input() id = ""
  @Input() nextId = ""

  @HostBinding("class.m-tableRow-is-warning")
  get isWarning(): boolean {
    return this.isSimilarToLocalSkill
  }

  imported = false

  externalLinkIcon = SvgHelper.path(SvgIcon.EXTERNAL_LINK)
  errorIcon = SvgHelper.path(SvgIcon.ERROR)

  get skill(): ApiSkillSummary | undefined {
    return this.searchResult?.skill
  }

  get isSimilarToLocalSkill(): boolean {
    return !!(this.searchResult?.similarToLocalSkill)
  }

  constructor(
    protected richSkillService: RichSkillService,
    protected toastService: ToastService
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
    if (this.skill && this.skill.libraryName) {
      // If similar to a local skill confirm import.
      if (!this.isSimilarToLocalSkill || confirm(ExternalSearchSkillRowComponent.IMPORT_CONFIRMATION_MSG)) {
        this.richSkillService.importSkill(
          this.skill.id,
          this.skill.libraryName
        ).subscribe(resp => {
          this.imported = true
          this.toastService.showToast(
            ExternalSearchSkillRowComponent.IMPORT_SUCCESS_TITLE,
            ExternalSearchSkillRowComponent.IMPORT_SUCCESS_MSG
          )
        })
      }
    }
  }
}
