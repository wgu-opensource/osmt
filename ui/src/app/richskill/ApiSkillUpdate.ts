import {ApiAlignment, ApiNamedReference, IAlignment, INamedReference} from "./ApiSkill"
import {PublishStatus} from "../PublishStatus"

export interface IStringListUpdate {
  add?: string[]
  remove?: string[]
}
export class ApiStringListUpdate implements IStringListUpdate {
  add?: string[]
  remove?: string[]

  constructor(add?: string[], remove?: string[]) {
    this.add = add
    this.remove = remove
  }
}

export interface IReferenceListUpdate {
  add?: INamedReference[]
  remove?: INamedReference[]
}
export class ApiReferenceListUpdate {
  add?: ApiNamedReference[]
  remove?: ApiNamedReference[]

  constructor(add?: ApiNamedReference[], remove?: ApiNamedReference[]) {
    this.add = add
    this.remove = remove
  }
}

export interface IAlignmentListUpdate {
  add?: ApiAlignment[]
  remove?: ApiAlignment[]
}
export class ApiAlignmentListUpdate {
  add?: ApiAlignment[]
  remove?: ApiAlignment[]

  constructor(add?: ApiAlignment[], remove?: ApiAlignment[]) {
    this.add = add
    this.remove = remove
  }
}



export interface IRichSkillUpdate {
  skillName?: string
  skillStatement?: string
  status?: PublishStatus
  category?: string
  keywords?: IStringListUpdate
  collections?: ApiStringListUpdate
  alignments?: IAlignmentListUpdate
  standards?: IAlignmentListUpdate
  certifications?: ApiReferenceListUpdate
  occupations?: ApiStringListUpdate
  employers?: ApiReferenceListUpdate
  authors?: IStringListUpdate
}

export class ApiSkillUpdate implements IRichSkillUpdate {
  skillName?: string
  skillStatement?: string
  status?: PublishStatus
  category?: string
  keywords?: ApiStringListUpdate
  collections?: ApiStringListUpdate
  alignments?: ApiAlignmentListUpdate
  standards?: ApiAlignmentListUpdate
  certifications?: ApiReferenceListUpdate
  occupations?: ApiStringListUpdate
  employers?: ApiReferenceListUpdate
  authors?: ApiStringListUpdate

  constructor({skillName, skillStatement, status, category, keywords, collections, alignments, certifications, standards, occupations, employers, authors}: IRichSkillUpdate) {
    this.skillName = skillName
    this.skillStatement = skillStatement
    this.status = status
    this.category = category
    this.keywords = keywords
    this.collections = collections
    this.alignments = alignments
    this.certifications = certifications
    this.standards = standards
    this.occupations = occupations
    this.employers = employers
    this.authors = authors
  }
}
