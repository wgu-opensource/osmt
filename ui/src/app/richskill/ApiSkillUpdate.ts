import {ApiNamedReference, INamedReference} from "./ApiSkill"
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


export interface IRichSkillUpdate {
  skillName?: string
  skillStatement?: string
  status?: PublishStatus
  category?: string
  keywords?: IStringListUpdate
  collections?: ApiStringListUpdate
  alignments?: ApiReferenceListUpdate
  certifications?: ApiReferenceListUpdate
  standards?: ApiReferenceListUpdate
  occupations?: ApiStringListUpdate
  employers?: ApiReferenceListUpdate
  author?: ApiNamedReference
}

export class ApiSkillUpdate implements IRichSkillUpdate {
  skillName?: string
  skillStatement?: string
  status?: PublishStatus
  category?: string
  keywords?: ApiStringListUpdate
  collections?: ApiStringListUpdate
  alignments?: ApiReferenceListUpdate
  certifications?: ApiReferenceListUpdate
  standards?: ApiReferenceListUpdate
  occupations?: ApiStringListUpdate
  employers?: ApiReferenceListUpdate
  author?: ApiNamedReference

  constructor({skillName, skillStatement, status, category, keywords, collections, alignments, certifications, standards, occupations, employers, author}: IRichSkillUpdate) {
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
    this.author = author
  }
}
