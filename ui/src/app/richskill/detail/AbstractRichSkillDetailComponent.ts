import {Component, Inject, LOCALE_ID, OnInit} from "@angular/core"
import {RichSkillService} from "../service/rich-skill.service"
import {ActivatedRoute} from "@angular/router"
import {ApiSkill, ApiUuidReference, INamedReference} from "../ApiSkill"
import {IDetailCardSectionData} from "../../detail-card/section/section.component"
import {Observable} from "rxjs"
import {PublishStatus} from "../../PublishStatus"
import {QuickLinksHelper} from "../../core/quick-links-helper"
import {dateformat} from "../../core/DateHelper"
import {Title} from "@angular/platform-browser";

@Component({template: ``})
export abstract class AbstractRichSkillDetailComponent extends QuickLinksHelper implements OnInit {


  uuidParam: string | null
  richSkill: ApiSkill | null = null
  loading = true

  skillLoaded: Observable<ApiSkill> | null = null

  constructor(
    protected richSkillService: RichSkillService,
    protected route: ActivatedRoute,
    protected titleService: Title,
    @Inject(LOCALE_ID) protected locale: string
  ) {
    super()
    this.uuidParam = this.route.snapshot.paramMap.get("uuid")
  }

  ngOnInit(): void {
    this.loadSkill()
  }

  abstract getCardFormat(): IDetailCardSectionData[]

  loadSkill(): void {

    this.skillLoaded = this.richSkillService.getSkillByUUID(this.uuidParam ?? "")
    this.skillLoaded.subscribe(skill => {
      this.richSkill = skill
      this.titleService.setTitle(`${skill.skillName} | Rich Skill Descriptor | ${this.whitelabel.toolName}`)
    })
  }

  getAuthor(): string {
    return this.richSkill?.author ?? ""
  }

  getSkillUuid(): string {
    return this.richSkill?.uuid ?? ""
  }

  getSkillName(): string {
    return this.richSkill?.skillName ?? ""
  }

  getPublishStatus(): PublishStatus {
    return this.richSkill?.status ?? PublishStatus.Draft
  }

  getIsExternallyShared(): boolean {
    return this.richSkill?.isExternallyShared ?? false
  }

  getImportedFrom(): string {
    return this.richSkill?.importedFrom ?? ""
  }

  getLibraryName(): string {
    return this.richSkill?.libraryName ?? ""
  }

  getSkillUrl(): string {
    return this.richSkill?.id ?? ""
  }

  getPublishedDate(): string {
    return this.richSkill?.publishDate
      ? dateformat(this.richSkill?.publishDate, this.locale)
      : ""
  }

  getArchivedDate(): string {
    return this.richSkill?.archiveDate
      ? dateformat(this.richSkill?.archiveDate, this.locale)
      : ""
  }

  joinKeywords(): string {
    const keywords = this.richSkill?.keywords || []
    return this.joinList("; ", keywords)
  }

  joinEmployers(): string {
    const employers = this.richSkill?.employers || []
    return this.joinGenericKeywords("; ", employers)
  }

  private joinList(delimeter: string, list: string[]): string {
    return list
      .filter(item => item)
      .join(delimeter)
  }

  private joinGenericKeywords(delimeter: string, keywords: INamedReference[]): string {
    const filteredList: string[] = keywords
      .map(keyword => (keyword.name ? keyword.name : keyword.id) as string)

    return this.joinList(delimeter, filteredList)
  }

  protected formatAssociatedCollections(isAuthorized: boolean): string {
    const targetUrl = (it: ApiUuidReference) => {
      return `/collections/${it.uuid}${isAuthorized ? "/manage" : ""}`
    }
    return this.richSkill?.collections
      ?.map(it => `<div><a class="t-link" href="${targetUrl(it)}">${it.name}</a></div>`)
      ?.join("<br>")
      ?? ""

  }
}
