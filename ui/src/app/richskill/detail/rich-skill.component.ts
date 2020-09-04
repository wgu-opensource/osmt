import { Component, OnInit } from "@angular/core"
import { RichSkill } from "../RichSkill"
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
  richSkill: RichSkill | null = null
  loading = true

  majorCodes: string[] | null = null
  minorCodes: string[] | null = null
  broadCodes: string[] | null = null
  detailedCodes: string[] | null = null
  jobRoleCodes: string[] | null = null


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

  private inferMajorCodes(codeBreakouts: JobCodeBreakout[]): string[] | null {
    const codes = codeBreakouts.flatMap(code => !!code?.majorCode() ? [code.majorCode() as string] : [])
    return this.dedupeCodes(codes)
  }

  private inferMinorCodes(codeBreakouts: JobCodeBreakout[]): string[] | null {
    const codes = codeBreakouts.flatMap(code => !!code?.minorCode() ? [code.minorCode() as string] : [])
    return this.dedupeCodes(codes)
  }

  private inferBroadCodes(codeBreakouts: JobCodeBreakout[]): string[] | null {
    const codes = codeBreakouts.flatMap(code => !!code?.broadCode() ? [code.broadCode() as string] : [])
    return this.dedupeCodes(codes)
  }

  private inferDetailedCodes(codeBreakouts: JobCodeBreakout[]): string[] | null {
    const codes = codeBreakouts.flatMap(code => !!code?.detailedCode() ? [code.detailedCode() as string] : [])
    return this.dedupeCodes(codes)
  }

  private inferJobRoleCodes(codeBreakouts: JobCodeBreakout[]): string[] | null {
    const codes = codeBreakouts.flatMap(code => !!code?.jobRoleCode() ? [code.jobRoleCode() as string] : [])
    return this.dedupeCodes(codes)
  }

  private dedupeCodes(codes: string[]): string[] | null {
    const withoutDupes = [...new Set(codes)]
    return withoutDupes.length > 0 ? withoutDupes : null
  }
}

