import {IKeyword} from "../keyword/Keyword"
import {IUuidDatabaseEntity} from "../ResponseEntity"
import {PublishStatus} from "../PublishStatus"

/**
 * The interface to a RichSkill response we get from the backend
 */
export interface IRichSkillResponse extends IUuidDatabaseEntity {
  creationDate?: string
  updateDate?: string
  skillName: string
  skillStatement: string
  author: string
  keywords: IKeyword[]
  publishStatus: PublishStatus
  category?: IKeyword
  certifications: IKeyword[]
}

export class RichSkill {
  uuid: string
  creationDate?: Date = undefined
  updateDate?: Date = undefined
  name: string
  statement: string
  author: string
  keywords: IKeyword[] = []
  publishStatus: PublishStatus
  category?: IKeyword
  certifications: IKeyword[]

  constructor(iRichSkill: IRichSkillResponse) {
    this.uuid = iRichSkill.id
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
  }
}
