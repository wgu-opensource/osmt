import {OccupationsSummary} from "../richskill/ApiSkillSummary";

export interface IJobCode {
  name?: string
  code: string
  id: number
  framework: string
}

export class JobCodeBreakout {
  code: string

  constructor(code: string) {
    this.code = code ? code : ""
  }

  private majorPart(): string {
    if (this.code.length < 2) {
      return ""
    }
    return this.code.substring(0, 2)
  }

  private minorPart(): string {
    if (this.code.length < 5) {
      return ""
    }
    return this.code.substring(3, 5)
  }

  private broadPart(): string {
    if (this.code.length < 6) {
      return ""
    }
    return this.code.substring(5, 6)
  }

  private detailedPart(): string {
    if (this.code.length < 7) {
      return ""
    }
    return this.code.substring(6, 7)
  }

  private jobRolePart(): string | null {
    const matcher = this.code.match(/\.(\d{2})$/)
    if (matcher && matcher[1]) {
      return matcher[1]
    } else {
      return null
    }
  }

  majorCode(): string | null {
    const majorPart = this.majorPart()
    return +majorPart > 0
      ? `${majorPart}-0000`
      : null
  }

  minorCode(): string | null {
    const majorPart = this.majorPart()
    const minorPart = this.minorPart()
    return +majorPart > 0 && +minorPart > 0
      ? `${this.majorPart()}-${minorPart}00`
      : null
  }

  broadCode(): string | null {
    const broadPart = this.broadPart()
    return +broadPart > 0
      ? `${this.majorPart()}-${this.minorPart()}${broadPart}0`
      : null
  }

  detailedCode(): string | null {
    const detailedPart = this.detailedPart()
    return +detailedPart > 0
      ? `${this.majorPart()}-${this.minorPart()}${this.broadPart()}${detailedPart}`
      : null
  }

  jobRoleCode(): string | null {
    const jobRolePart = this.jobRolePart()
    return jobRolePart
      ? `${this.majorPart()}-${this.minorPart()}${this.broadPart()}${this.detailedPart()}.${jobRolePart}`
      : null
  }
}

export class OccupationsFormatter {

  constructor(codes: (IJobCode | OccupationsSummary)[]) {
    this.codes = codes
  }

  private codes: (IJobCode | OccupationsSummary)[]


  private static joinList(delimeter: string, list: string[]): string {
    return list
      .filter(item => item)
      .join(delimeter)
  }

  private static dedupeCodes(codes: string[]): string[] | null {
    const withoutDupes = [...new Set(codes)]
    return withoutDupes.length > 0 ? withoutDupes : null
  }

  html(): string {
    const codeBreakouts: JobCodeBreakout[] = this.getBreakouts()

    const majorCodes = this.majorGroups(codeBreakouts)
    const minorCodes = this.minorGroups(codeBreakouts)
    const broadCodes = this.broadGroups(codeBreakouts)
    const detailedCodes = this.detailedGroups(codeBreakouts)
    const jobRoleCodes = this.oNetJobRoles(codeBreakouts)

    let html = ""

    if (majorCodes) {
      html += `<h4 class="t-type-bodyBold">Major Groups</h4><p>${majorCodes}</p>`
    }

    if (minorCodes) {
      html += `<h4 class="t-type-bodyBold">Minor Groups</h4><p>${minorCodes}</p>`
    }

    if (broadCodes) {
      html += `<h4 class="t-type-bodyBold">Broad Occupations</h4><p>${broadCodes}</p>`
    }

    if (detailedCodes) {
      html += `<h4 class="t-type-bodyBold">Detailed Occupations</h4><p>${detailedCodes}</p>`
    }

    if (jobRoleCodes) {
      html += `<h4 class="t-type-bodyBold">O*NET Job Roles</h4><p>${jobRoleCodes}</p>`
    }

    return html
  }

  private getBreakouts(): JobCodeBreakout[] {
    return this.codes.map(code => new JobCodeBreakout(code.code))
  }

  majorGroups(codeBreakouts: JobCodeBreakout[] = this.getBreakouts()): string {
    const codes = codeBreakouts.flatMap(code => !!code?.majorCode() ? [code.majorCode() as string] : [])
    return OccupationsFormatter.dedupeCodes(codes)?.join("; ") || ""
  }

  minorGroups(codeBreakouts: JobCodeBreakout[] = this.getBreakouts()): string {
    const codes = codeBreakouts.flatMap(code => !!code?.minorCode() ? [code.minorCode() as string] : [])
    return OccupationsFormatter.joinList("; ", OccupationsFormatter.dedupeCodes(codes)  || [])
  }

  broadGroups(codeBreakouts: JobCodeBreakout[] = this.getBreakouts()): string {
    const codes = codeBreakouts.flatMap(code => !!code?.broadCode() ? [code.broadCode() as string] : [])
    return OccupationsFormatter.joinList("; ", OccupationsFormatter.dedupeCodes(codes)  || [])
  }

  detailedGroups(codeBreakouts: JobCodeBreakout[] = this.getBreakouts()): string {
    const codes = codeBreakouts.flatMap(code => !!code?.detailedCode() ? [code.detailedCode() as string] : [])
    return OccupationsFormatter.joinList("; ", OccupationsFormatter.dedupeCodes(codes)  || [])
  }

  oNetJobRoles(codeBreakouts: JobCodeBreakout[] = this.getBreakouts()): string {
    const codes = codeBreakouts.flatMap(code => !!code?.jobRoleCode() ? [code.jobRoleCode() as string] : [])
    return OccupationsFormatter.joinList("; ", OccupationsFormatter.dedupeCodes(codes)  || [])
  }

}
