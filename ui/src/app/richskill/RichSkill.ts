import {IKeyword} from "../keyword/Keyword"
import { IJobCode } from "../jobcode/Jobcode"
import {PublishStatus} from "../PublishStatus"

/**
 * The interface to a RichSkill response we get from the backend
 */
export interface IRichSkillResponse {
  id: string
  uuid: string
  creationDate?: string
  updateDate?: string
  skillName: string
  skillStatement: string
  author: IKeyword
  keywords: IKeyword[]
  publishStatus: PublishStatus
  category?: IKeyword
  certifications: IKeyword[]
  alignments: IKeyword[]
  standards: IKeyword[]
  type: string
  employers: IKeyword[]
  occupations: IJobCode[]
 }

export class RichSkill {
  id: string
  uuid: string
  creationDate?: Date = undefined
  updateDate?: Date = undefined
  name: string
  statement: string
  author: IKeyword
  keywords: IKeyword[]
  publishStatus: PublishStatus
  category?: IKeyword
  certifications: IKeyword[]
  alignments: IKeyword[]
  standards: IKeyword[]
  type: string
  skillName: string
  employers: IKeyword[]
  occupations: IJobCode[]

  constructor(iRichSkill: IRichSkillResponse) {
    this.id = iRichSkill.id
    this.uuid = iRichSkill.uuid
    if (iRichSkill.creationDate) {
      this.creationDate = new Date(iRichSkill.creationDate)
    }
    if (iRichSkill.updateDate) {
      this.updateDate = new Date(iRichSkill.updateDate)
    }
    this.name = iRichSkill.skillName
    this.statement = iRichSkill.skillStatement
    this.author = iRichSkill.author
    this.keywords = iRichSkill.keywords
    this.publishStatus = iRichSkill.publishStatus
    this.category = iRichSkill.category
    this.certifications = iRichSkill.certifications
    this.alignments = iRichSkill.alignments
    this.standards = iRichSkill.standards
    this.type = iRichSkill.type
    this.skillName = iRichSkill.skillName
    this.employers = iRichSkill.employers
    this.occupations = iRichSkill.occupations
  }
}
