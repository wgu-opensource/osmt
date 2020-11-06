import {PublishStatus} from "../PublishStatus"
import {IJobCode} from "../job-codes/Jobcode";

export interface IApiSkillSummary {
  id: string
  uuid: string
  status: PublishStatus
  skillName: string
  skillStatement: string
  category: string
  keywords: string[]
  occupations: IJobCode[]
}

export class ApiSkillSummary implements IApiSkillSummary {
  id: string
  uuid: string
  status: PublishStatus
  skillName: string
  skillStatement: string
  category: string
  keywords: string[]
  occupations: IJobCode[]

  constructor({id, uuid, category, occupations, skillName,
                status, keywords, skillStatement}: IApiSkillSummary) {
    this.id = id
    this.uuid = uuid
    this.category = category
    this.occupations = occupations
    this.skillName = skillName
    this.status = status
    this.keywords = keywords
    this.skillStatement = skillStatement
  }
}


// move to collections space when created
export interface ICollectionSummary {
  id?: number
  uuid?: string
  name?: string
  skillCount?: number
  status?: PublishStatus
}

export class ApiCollectionSummary implements ICollectionSummary {
  id?: number
  uuid?: string
  name?: string
  skillCount?: number
  status?: PublishStatus

  constructor(options: ICollectionSummary)  {
    Object.assign(this, options)
  }
}
