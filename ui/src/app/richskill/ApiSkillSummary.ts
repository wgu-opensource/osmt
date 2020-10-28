import {PublishStatus} from "../PublishStatus"

export interface IApiSkillSummary {
  db_id: number
  uuid: string
  id: string
  skillName: string
  skillStatement: string
  category: string
  author?: string
  status: PublishStatus
  keywords: string[]
  occupations: OccupationsSummary[]
  standards: string[]
  certifications: string[]
  employers: string[]
  alignments: string[]
  collections: CollectionSummary
}

export class ApiSkillSummary implements IApiSkillSummary {
  "db_id": number
  uuid: string
  id: string
  skillName: string
  skillStatement: string
  category: string
  author?: string
  status: PublishStatus
  keywords: string[]
  occupations: OccupationsSummary[]
  standards: string[]
  certifications: string[]
  employers: string[]
  alignments: string[]
  collections: CollectionSummary

  constructor({id, uuid, alignments, category, certifications,
                collections, employers, occupations, skillName,
                status, keywords, standards,
                skillStatement, db_id, author}: IApiSkillSummary) {
    this.id = id
    this.uuid = uuid
    this.alignments = alignments
    this.category = category
    this.certifications = certifications
    this.collections = collections
    this.employers = employers
    this.occupations = occupations
    this.skillName = skillName
    this.status = status
    this.keywords = keywords
    this.standards = standards
    this.skillStatement = skillStatement
    this.db_id = db_id
    this.author = author
  }
}

export interface OccupationsSummary {
  code: string
  major?: string
  minor?: string
  broad?: string
  detailed?: string
  name?: string
  description?: string
  framework?: string
  url?: string
}

// move to collections space when created
export interface CollectionSummary {
  id: number
  uuid: string
  name: string
  publishStatus: PublishStatus
  skillIds: string[]
  skillCount?: number
  author?: string
}
