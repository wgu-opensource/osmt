import {Component, Inject, LOCALE_ID, OnInit} from "@angular/core"
import {RichSkillService} from "../service/rich-skill.service"
import {ActivatedRoute} from "@angular/router"
import {ApiSkill, INamedReference} from "../ApiSkill"
import {IDetailCardSectionData} from "../../detail-card/section/section.component"
import {formatDate} from "@angular/common"
import {Observable} from "rxjs"

@Component({template: ""})
export abstract class AbstractRichSkillDetailComponent implements OnInit {

  uuidParam: string | null
  richSkill: ApiSkill | null = null
  loading = true

  skillLoaded: Observable<ApiSkill> | null = null

  constructor(
    protected richSkillService: RichSkillService,
    protected route: ActivatedRoute,
    @Inject(LOCALE_ID) protected locale: string
  ) {
    this.uuidParam = this.route.snapshot.paramMap.get("uuid")
  }

  ngOnInit(): void {
    this.skillLoaded = this.richSkillService.getSkillByUUID(this.uuidParam ?? "")
    this.skillLoaded.subscribe(skill => { this.richSkill = skill })
  }

  abstract getCardFormat(): IDetailCardSectionData[]

  getAuthor(): string {
    return this.richSkill?.author?.name ?? ""
  }

  getSkillUuid(): string {
    return this.richSkill?.uuid ?? ""
  }

  getSkillName(): string {
    return this.richSkill?.skillName ?? ""
  }

  getPublishedDate(): string {
    return this.richSkill?.publishDate
      ? this.getDateFormat(this.richSkill?.publishDate)
      : ""
  }

  getArchivedDate(): string {
    return this.richSkill?.archiveDate
      ? this.getDateFormat(this.richSkill?.archiveDate)
      : ""
  }

  getDateFormat(date?: Date): string {
    if (date) {
      return formatDate(date, "MMM dd yyyy", this.locale)
    } else {
      return ""
    }
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

  protected formatAssociatedCollections(): string {
    return this.richSkill?.collections
      ?.map(standard => `<div>${standard}</div>`)
      ?.join("<br>")
      ?? ""

  }
}
