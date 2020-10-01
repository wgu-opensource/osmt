import {IJobCode} from "../jobcode/Jobcode"
import {PublishStatus} from "../PublishStatus"


/**
 * The interface to a ApiSkill response we get from the backend
 */

export interface INamedReference {
  id?: string
  name?: string
}
export class ApiNamedReference {
  id?: string
  name?: string
  constructor(reference: INamedReference) {
    this.id = reference.id
    this.name = reference.name
  }
}

export interface ISkill {
  id: string
  uuid: string
  creationDate?: string
  updateDate?: string
  type: string
  status: PublishStatus
  skillName: string
  skillStatement: string
  category?: string
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
  type: string
  status: PublishStatus
  skillName: string
  skillStatement: string
  category?: string
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
    this.skillName = iRichSkill.skillName
    this.skillStatement = iRichSkill.skillStatement
    this.author = iRichSkill.author
    this.keywords = iRichSkill.keywords
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
