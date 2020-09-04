export enum JobCodeType {
  Major,
  Minor,
  Broad,
  Detailed,
  JobRole
}

export class JobcodeParser {

  /**
   * Infer the type of job the a code represents.  The format of the code is JJ-NNBD.RR
   *
   * JJ - BLS Major
   * NN - BLS Minor
   * B - BLS Broad
   * D - BLS Detailed
   * RR - O*Net Job Role (optional)
   *
   * The presence of ".RR" at the end of the token signifies this code is a Job Role.  Otherwise, starting at the end, find the first
   * non-zero digit, it's position signifies the type of job.
   *
   */
  parseCode(code: string): JobCodeType | undefined {
    // These checks are cascading, if the broad part passes it's check
    // then the minor and major part will implicitly too and so on
    const majorPart: boolean = +code.substring(0, 2) > 0
    const minorPart: boolean = +code.substring(3, 5) > 0
    const broadPart: boolean = +code.substring(5, 6) > 0
    const detailedPart: boolean = +code.substring(6, 7) > 0
    const jobRolePart: boolean = !!(code.match(/\.\d{2}$/)) // match if the end of the code has the job role bit present

    if (jobRolePart) {
      return JobCodeType.JobRole
    } else if (detailedPart ) {
      return JobCodeType.Detailed
    } else if (broadPart) {
      return JobCodeType.Broad
    } else if (minorPart) {
      return JobCodeType.Minor
    } else if (majorPart) {
      return JobCodeType.Major
    } else {
      return undefined
    }
  }
}
