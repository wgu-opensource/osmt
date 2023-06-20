export type JobCodeLevel =  "Major" | "Minor" | "Broad" | "Detailed" | "Onet"

export interface IJobCode {
  id?: number
  targetNodeName?: string
  code: string
  targetNode?: number
  frameworkName?: string
  jobCodeLevelAsNumber?: number
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
  id?: number
  code = ""
  targetNodeName?: string
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

  constructor({code, targetNodeName, targetNode, frameworkName, level, parents}: IJobCode) {
    this.code = code
    this.targetNodeName = targetNodeName
    this.targetNode = targetNode
    this.frameworkName = frameworkName
    this.level = level
    this.parents = parents
  }
}
