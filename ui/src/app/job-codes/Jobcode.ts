export type JobCodeLevel =  "Major" | "Minor" | "Broad" | "Detailed" | "Onet"

export interface IJobCode {
  name?: string
  code: string
  id?: number
  framework?: string
  level?: JobCodeLevel
  parents?: IJobCode[]
  major?: string
  majorCode?: string
  minor?: string
  minorCode?: string
  broad?: string
  broadCode?: string
  detailed?: string
  url?: string
}

export class ApiJobCode implements IJobCode {
  code = ""
  name?: string
  id?: number
  framework?: string
  level?: JobCodeLevel
  parents?: IJobCode[]

  constructor(o?: IJobCode) {
    if (o !== undefined) {
      Object.assign(this, o)
    }
  }
}
