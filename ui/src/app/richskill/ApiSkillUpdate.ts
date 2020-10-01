import {INamedReference} from "./RichSkill"
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

export interface IRichSkillUpdate {
  skillName?: string
  skillStatement?: string
  status?: PublishStatus
  category?: string
  keywords?: IStringListUpdate
  alignments?: IReferenceListUpdate
  certifications?: IReferenceListUpdate
  standards?: IReferenceListUpdate
  occupations?: IStringListUpdate
  employers?: IReferenceListUpdate
  author?: INamedReference
}

export class ApiSkillUpdate {
  skillName?: string
  skillStatement?: string
  status?: PublishStatus
  category?: string
  keywords?: ApiStringListUpdate
  alignments?: IReferenceListUpdate
  certifications?: IReferenceListUpdate
  standards?: IReferenceListUpdate
  occupations?: IStringListUpdate
  employers?: IReferenceListUpdate
  author?: INamedReference

}
