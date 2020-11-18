import {PublishStatus} from "../PublishStatus"
import {IJobCode} from "../job-codes/Jobcode"

export class ApiSkillSummary {
  id: string
  uuid: string
  status: PublishStatus
  archiveDate?: string
  publishDate?: string
  skillName: string
  skillStatement: string
  category: string
  keywords: string[]
  occupations: IJobCode[]

  constructor(
    id: string,
    uuid: string,
    category: string,
    occupations: IJobCode[],
    skillName: string,
    status: PublishStatus,
    keywords: string[],
    skillStatement: string,
    archiveDate?: string,
    publishDate?: string
  ) {
    this.id = id
    this.uuid = uuid
    this.category = category
    this.occupations = occupations
    this.skillName = skillName
    this.status = status
    this.keywords = keywords
    this.skillStatement = skillStatement
    this.archiveDate = archiveDate
    this.publishDate = publishDate
  }
}


// move to collections space when created
export interface ICollectionSummary {
  id: string
  uuid: string
  name: string
  skillCount?: number
  status?: PublishStatus
  archiveDate?: string
  publishDate?: string
}

export class ApiCollectionSummary implements ICollectionSummary {
  id: string
  uuid: string
  name: string
  skillCount?: number
  status?: PublishStatus
  archiveDate?: string
  publishDate?: string

  constructor({id, uuid, name, skillCount, status, archiveDate, publishDate}: ICollectionSummary)  {
    this.id = id
    this.uuid = uuid
    this.name = name
    this.skillCount = skillCount
    this.status = status
    this.archiveDate = archiveDate
    this.publishDate = publishDate
  }
}
