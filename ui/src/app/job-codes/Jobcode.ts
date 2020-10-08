import { min } from 'rxjs/operators'

export interface IJobCode {
  name?: string
  code: string
  id: number
  framework: string
}

export class JobCodeBreakout {
  code: string
  
  constructor(code: string) {
    this.code = code
    this.check()
  }
  
  check(): void {
    console.log(`raw=[${this.code}] reconstructed=[${this.majorPart()}-${this.minorPart()}${(this.broadPart())}${(this.detailedPart())}.${(this.jobRolePart())}]`)
  }
  
  private majorPart(): string {
    return this.code.substring(0, 2)
  }
  
  private minorPart(): string {
    return this.code.substring(3, 5)
  }
  
  private broadPart(): string {
    return this.code.substring(5, 6)
  }
  
  private detailedPart(): string {
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
  
  private codes: IJobCode[]
  
  constructor(codes: IJobCode[]) {
    this.codes = codes
  }
  
  html(): string {
    const codeBreakouts: JobCodeBreakout[] = this.codes.map((code: IJobCode) => new JobCodeBreakout(code.code))
    
    const majorCodes = this.inferMajorCodes(codeBreakouts)
    const minorCodes = this.inferMinorCodes(codeBreakouts)
    const broadCodes = this.inferBroadCodes(codeBreakouts)
    const detailedCodes = this.inferDetailedCodes(codeBreakouts)
    const jobRoleCodes = this.inferJobRoleCodes(codeBreakouts)
    
    return `
    <span class="t-type-bodyBold">Major codes</span><br>
    ${majorCodes}<br>
    <span class="t-type-bodyBold">Minor codes</span><br>
    ${minorCodes}<br>
    <span class="t-type-bodyBold">Broad codes</span><br>
    ${broadCodes}<br>
    <span class="t-type-bodyBold">Detailed codes</span><br>
    ${detailedCodes}<br>
    <span class="t-type-bodyBold">O*NET Job Roles</span><br>

    ${jobRoleCodes}
    `
  }
  
  
  private joinList(delimeter: string, list: string[]): string {
    return list
    .filter(item => item)
    .join(delimeter)
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
