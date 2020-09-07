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
      return +matcher[1] > 0 ? matcher[1] : null // even if defined, if zero then don't display it
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
