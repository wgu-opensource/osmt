import {IJobCode, JobCodeBreakout, OccupationsFormatter} from "../job-codes/Jobcode"
import {PublishStatus} from "../PublishStatus"


/**
 * The interface to a ApiSkill response we get from the backend
 */

export interface INamedReference {
  id?: string
  name?: string
}
export class ApiNamedReference implements INamedReference {
  id?: string
  name?: string
  constructor(reference: INamedReference) {
    this.id = reference.id
    this.name = reference.name
  }

  equals(other: ApiNamedReference): boolean {
    return this.id === other.id && this.name === other.name
  }
}

export enum KeywordType {
  Category = "category",
  Keyword = "keyword",
  Standard = "standard",
  Certification = "certification",
  Alignment = "alignment",
  Employer = "employer",
  Author = "author"
}

export interface IUuidReference {
  uuid: string
  name: string
}
export class ApiUuidReference implements IUuidReference {
  uuid: string = ""
  name: string = ""

  constructor(it: IUuidReference) {
    Object.assign(this, it)
  }
}

export interface ISkill {
  id: string
  uuid: string
  creationDate: string
  updateDate: string
  archiveDate?: string
  publishDate?: string
  type: string
  status: PublishStatus
  skillName: string
  skillStatement: string
  category?: string
  collections: IUuidReference[]
  keywords: string[]
  alignments: INamedReference[]
  standards: INamedReference[]
  certifications: INamedReference[]
  occupations: IJobCode[]
  employers: INamedReference[]
  author: INamedReference
}

export class ApiSkill {
  id: string
  uuid: string
  creationDate?: Date = undefined
  updateDate?: Date = undefined
  publishDate?: Date = undefined
  archiveDate?: Date = undefined
  type: string
  status: PublishStatus
  skillName: string
  skillStatement: string
  category?: string
  collections: IUuidReference[]
  keywords: string[]
  alignments: INamedReference[]
  standards: INamedReference[]
  certifications: INamedReference[]
  occupations: IJobCode[]
  employers: INamedReference[]
  author: INamedReference

  constructor(iRichSkill: ISkill) {
    this.id = iRichSkill.id
    this.uuid = iRichSkill.uuid
    if (iRichSkill.creationDate) {
      this.creationDate = new Date(iRichSkill.creationDate)
    }
    if (iRichSkill.updateDate) {
      this.updateDate = new Date(iRichSkill.updateDate)
    }
    if (iRichSkill.publishDate) {
      this.publishDate = new Date(iRichSkill.publishDate)
    }
    if (iRichSkill.archiveDate) {
      this.archiveDate = new Date(iRichSkill.archiveDate)
    }
    this.skillName = iRichSkill.skillName
    this.skillStatement = iRichSkill.skillStatement
    this.author = iRichSkill.author
    this.keywords = iRichSkill.keywords
    this.collections = iRichSkill.collections
    this.status = iRichSkill.status
    this.category = iRichSkill.category
    this.certifications = iRichSkill.certifications
    this.alignments = iRichSkill.alignments
    this.standards = iRichSkill.standards
    this.type = iRichSkill.type
    this.employers = iRichSkill.employers
    this.occupations = iRichSkill.occupations
  }
}

export enum ApiSortOrder {
  SkillAsc = "skill.asc",
  SkillDesc = "skill.desc",
  NameAsc = "name.asc",
  NameDesc = "name.desc"
}


export enum AuditOperationType {
  Insert = "Insert",
  Update = "Update",
  PublishStatusChange = "PublishStatusChange"
}

export interface IChange {
  fieldName: string
  old: string
  new: string
}

export interface IAuditLog {
  creationDate: string
  operationType: AuditOperationType
  user: string
  changedFields: IChange[]
}

export class ApiAuditLog {
  creationDate: Date
  operationType: AuditOperationType
  user: string
  changedFields: IChange[]

  constructor({creationDate, operationType, user, changedFields}: IAuditLog) {
    this.creationDate = new Date(creationDate)
    this.operationType = operationType
    this.user = user
    this.changedFields = changedFields
  }
  
  isPublishStatusChange(): boolean {
    return this.operationType === AuditOperationType.PublishStatusChange
  }
}
