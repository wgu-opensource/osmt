export type JobCodeLevel =  "Major" | "Minor" | "Broad" | "Detailed" | "Onet"

export interface IJobCode {
  targetNodeName?: string
  code: string
  targetNode?: number
  frameworkName?: string
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
  targetNodeName?: string
  targetNode?: number
  frameworkName?: string
  level?: JobCodeLevel
  parents?: IJobCode[]

  constructor(o?: IJobCode) {
    if (o !== undefined) {
      Object.assign(this, o)
    }
  }
}

export interface IJobCodeUpdate {
  targetNodeName?: string
  code: string
  targetNode?: number
  frameworkName?: string
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

export class ApiJobCodeUpdate implements IJobCodeUpdate {
  code = ""
  targetNodeName?: string
  targetNode?: number
  frameworkName?: string
  level?: JobCodeLevel
  parents?: IJobCode[]

  constructor(o?: IJobCode) {
    if (o !== undefined) {
      Object.assign(this, o)
    }
  }
}
