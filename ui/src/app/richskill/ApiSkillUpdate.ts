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
  alignments?: IReferenceListUpdate
  certifications?: IReferenceListUpdate
  standards?: IReferenceListUpdate
  occupations?: IStringListUpdate
  employers?: IReferenceListUpdate
  author?: INamedReference
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

}
