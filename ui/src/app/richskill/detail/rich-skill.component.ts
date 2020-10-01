import { Component, OnInit } from "@angular/core"
import {ApiNamedReference, INamedReference, ApiSkill} from "../ApiSkill"
import { RichSkillService } from "../service/rich-skill.service"
import { ActivatedRoute } from "@angular/router"
import {JobCodeBreakout, IJobCode} from "../../jobcode/Jobcode"

@Component({
  selector: "app-richskill",
  templateUrl: "./rich-skill.component.html",
  styleUrls: ["./rich-skill.component.css"]
})
export class RichSkillComponent implements OnInit {

  uuidParam: string | null
  richSkill: ApiSkill | null = null
  loading = true

  majorCodes = ""
  minorCodes = ""
  broadCodes = ""
  detailedCodes = ""
  jobRoleCodes = ""


  constructor(private richSkillService: RichSkillService, private route: ActivatedRoute) {
    this.route.params.subscribe(params => console.log(params))
    console.log(this.route.snapshot.paramMap.get("uuid"))
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
        this.inferCodes()
        this.loading = false
      },
      (error) => {
        console.log(`Error loading skill: ${error}`)
        this.loading = false
      }

    )  }

  inferCodes(): void {
    const occupations = this.richSkill?.occupations

    if (occupations) {

      const codeBreakouts: JobCodeBreakout[] = occupations
        .map((code: IJobCode) => new JobCodeBreakout(code.code))

      this.majorCodes = this.inferMajorCodes(codeBreakouts)
      this.minorCodes = this.inferMinorCodes(codeBreakouts)
      this.broadCodes = this.inferBroadCodes(codeBreakouts)
      this.detailedCodes = this.inferDetailedCodes(codeBreakouts)
      this.jobRoleCodes = this.inferJobRoleCodes(codeBreakouts)
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

  private inferMajorCodes(codeBreakouts: JobCodeBreakout[]): string {
    const codes = codeBreakouts.flatMap(code => !!code?.majorCode() ? [code.majorCode() as string] : [])
    return this.dedupeCodes(codes)?.join("; ") || ""
  }

  private inferMinorCodes(codeBreakouts: JobCodeBreakout[]): string {
    const codes = codeBreakouts.flatMap(code => !!code?.minorCode() ? [code.minorCode() as string] : [])
    return this.joinList("; ", this.dedupeCodes(codes)  || [])
  }

  private inferBroadCodes(codeBreakouts: JobCodeBreakout[]): string {
    const codes = codeBreakouts.flatMap(code => !!code?.broadCode() ? [code.broadCode() as string] : [])
    return this.joinList("; ", this.dedupeCodes(codes)  || [])
  }

  private inferDetailedCodes(codeBreakouts: JobCodeBreakout[]): string {
    const codes = codeBreakouts.flatMap(code => !!code?.detailedCode() ? [code.detailedCode() as string] : [])
    return this.joinList("; ", this.dedupeCodes(codes)  || [])
  }

  private inferJobRoleCodes(codeBreakouts: JobCodeBreakout[]): string {
    const codes = codeBreakouts.flatMap(code => !!code?.jobRoleCode() ? [code.jobRoleCode() as string] : [])
    return this.joinList("; ", this.dedupeCodes(codes)  || [])
  }

  private dedupeCodes(codes: string[]): string[] | null {
    const withoutDupes = [...new Set(codes)]
    return withoutDupes.length > 0 ? withoutDupes : null
  }
}

