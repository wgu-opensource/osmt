import { Component, Inject, InjectionToken, LOCALE_ID, OnInit } from "@angular/core"
import {ApiNamedReference, INamedReference, ApiSkill} from "../ApiSkill"
import { RichSkillService } from "../service/rich-skill.service"
import { ActivatedRoute } from "@angular/router"
import {JobCodeBreakout, IJobCode, OccupationsFormatter} from "../../job-codes/Jobcode"
import { IDetailCardSectionData } from "src/app/detail-card/section/section.component"
import { formatDate } from "@angular/common"

@Component({
  selector: "app-richskill",
  templateUrl: "./rich-skill.component.html"
})
export class RichSkillComponent implements OnInit {

  uuidParam: string | null
  richSkill: ApiSkill | null = null
  loading = true

  constructor(
    private richSkillService: RichSkillService,
    private route: ActivatedRoute,
    @Inject(LOCALE_ID) public locale: string
    ) {
      this.uuidParam = this.route.snapshot.paramMap.get("uuid")
    }

    ngOnInit(): void {
      if (this.uuidParam !== null) {
        this.getSkill(this.uuidParam)
      }
    }

    getSkill(uuid: string): void {
      this.richSkillService.getSkillByUUID(uuid).subscribe(
        skill => {
          this.richSkill = skill
          this.loading = false
        },
        (error) => {
          console.log(`Error loading skill: ${error}`)
          this.loading = false
        }
        )
      }

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
        return this.getDateFormat(this.richSkill?.publishDate)
      }

      getArchivedDate(): string {
        return this.getDateFormat(this.richSkill?.archiveDate)
      }

      getDateFormat(date?: Date): string {
        if (date) {
          return formatDate(date, "MMM dd yyyy", this.locale)
        } else {
          return ""
        }
      }

      getCardFormat(): IDetailCardSectionData[] {
        return [
          {
            label: "Skill Statement",
            bodyHtml: this.richSkill?.skillStatement ?? ""
          }, {
            label: "Category",
            bodyHtml: this.richSkill?.category ?? ""
          }, {
            label: "Keywords",
            bodyHtml: this.richSkill?.keywords?.join("; ") ?? ""
          }, {
            label: "Associated Collections",
            bodyHtml: this.richSkill?.collections?.join("; ") ?? ""
          }, {
            label: "Standards",
            bodyHtml: this.richSkill?.standards?.map(standard => standard.name)?.join("; ") ?? ""
          }, {
            label: "Certifications",
            bodyHtml: this.richSkill?.certifications?.map(cert => cert.name)?.join("; ") ?? ""
          }, {
            label: "Occupations",
            bodyHtml: new OccupationsFormatter(this.richSkill?.occupations ?? []).html()
          }, {
            label: "Employers",
            bodyHtml: this.richSkill?.employers?.map(employer => employer.name)?.join("; ") ?? ""
          }, {
            label: "Alignment",
            bodyHtml:
                this.richSkill?.alignments?.map(alignment => this.prepareAlignmentLink(alignment))?.join("; ") ?? ""
          }
        ]
      }
    private prepareAlignmentLink({name, id}: INamedReference): string {
        const displayText = name ? name : (id ? id : "") // prefer name, but use url if necessary


        if (id) {
            return `<a target="_blank" href=${id}>${displayText}</a>`
        } else {
            return displayText
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
    }
