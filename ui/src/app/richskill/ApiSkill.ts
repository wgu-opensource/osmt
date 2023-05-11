import {ApiJobCode, IJobCode} from "../metadata/job-codes/Jobcode"
import {PublishStatus} from "../PublishStatus"


/**
 * The interface to a ApiSkill response we get from the backend
 */

export interface IRef {
  toString(): string
}

export interface INamedReference {
  id?: string
  name?: string
}
export interface IAlignment {
  id?: string
  skillName?: string
  isPartOf?: INamedReference
}

export class ApiNamedReference implements INamedReference {
  id?: string
  name?: string

  constructor(reference: INamedReference) {
    this.id = reference.id
    this.name = reference.name
  }

  equals(other?: ApiNamedReference): boolean {
    return this.id === other?.id && this.name === other?.name
  }
  static formatRef(ref: INamedReference): string {
    return ref.name ?? ""
  }
  static fromString(textValue: string): ApiNamedReference | undefined {
    const val: string = textValue.trim()
    if (val.length < 1) {
      return undefined
    }

    if (val.indexOf("://") !== -1) {
      return new ApiNamedReference({id: val})
    } else {
      return new ApiNamedReference({name: val})
    }
  }
}
export class ApiAlignment implements IAlignment {
  id?: string
  skillName?: string
  isPartOf?: ApiNamedReference

  constructor(reference: IAlignment) {
    this.id = reference.id
    this.skillName = reference.skillName
    this.isPartOf = reference.isPartOf ? new ApiNamedReference(reference.isPartOf) : undefined
  }
  equals(other: ApiAlignment): boolean {
    return this.id === other.id && this.skillName === other.skillName &&
      (this.isPartOf?.equals(other.isPartOf) ?? other.isPartOf === undefined)
  }
  static formatRef(ref: IAlignment): string {
    return ref.skillName ?? ""
  }
  static fromString(textValue: string): ApiAlignment | undefined {
    const val: string = textValue.trim()
    if (val.length < 1) {
      return undefined
    }

    if (val.indexOf("://") !== -1) {
      return new ApiAlignment({id: val})
    } else {
      return new ApiAlignment({skillName: val})
    }
  }
}

export interface IKeywordCount {
  keyword: IAlignment|INamedReference|string
  count: number
}

export class KeywordCount implements IKeywordCount {
  keyword: IAlignment|INamedReference|string
  count: number

  constructor(reference: KeywordCount) {
    this.keyword = reference.keyword
    this.count = reference.count
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
  categories: string[]
  collections: IUuidReference[]
  keywords: string[]
  alignments: IAlignment[]
  standards: IAlignment[]
  certifications: INamedReference[]
  occupations: IJobCode[]
  employers: INamedReference[]
  authors: string[]
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
  categories: string[]
  collections: IUuidReference[]
  keywords: string[]
  alignments: IAlignment[]
  standards: IAlignment[]
  certifications: INamedReference[]
  occupations: IJobCode[]
  employers: INamedReference[]
  authors: string[]

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
    this.authors = iRichSkill.authors?.map(it => it) ?? null
    this.keywords = iRichSkill.keywords?.map(it => it) ?? null
    this.collections = iRichSkill.collections?.map(it => it) ?? null
    this.status = iRichSkill.status
    this.categories = iRichSkill.categories?.map(it => it) ?? null
    this.certifications = iRichSkill.certifications?.map(it => new ApiNamedReference(it)) ?? null
    this.alignments = iRichSkill.alignments?.map(it => new ApiAlignment(it)) ?? null
    this.standards = iRichSkill.standards?.map(it => new ApiAlignment(it)) ?? null
    this.type = iRichSkill.type
    this.employers = iRichSkill.employers?.map(it => new ApiNamedReference(it)) ?? null
    this.occupations = iRichSkill.occupations?.map(it => new ApiJobCode(it)) ?? null
  }

  get sortedAlignments(): IAlignment[] {
    return [...this.alignments].sort((a,b) => {
      const fwk = a.isPartOf ? a.isPartOf?.name?.localeCompare(b.isPartOf?.name ?? "") : 1
      const name = a.skillName ? a.skillName.localeCompare(b.skillName ?? "") : 1
      return fwk || name
    })
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
