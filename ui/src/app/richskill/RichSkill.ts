import {Keyword} from "../keyword/Keyword"
import {IUuidDatabaseEntity} from "../ResponseEntity"
import {PublishStatus} from "../PublishStatus"

/**
 * The interface to a RichSkill response we get from the backend
 */
export interface IRichSkillResponse extends IUuidDatabaseEntity {
  creationDate: string
  updateDate: string
  name: string
  statement: string
  author: string
  keywords: Keyword[]
  publishStatus: PublishStatus
  category?: Keyword
}

export class RichSkill {
  uuid: string
  creationDate: Date
  updateDate: Date
  name: string
  statement: string
  author: string
  keywords: Keyword[]
  publishStatus: PublishStatus
  category?: Keyword

  constructor(iRichSkill: IRichSkillResponse) {
    this.uuid = iRichSkill.uuid
    this.creationDate = new Date(iRichSkill.creationDate)
    this.updateDate = new Date(iRichSkill.updateDate)
    this.name = iRichSkill.name
    this.statement = iRichSkill.statement
    this.author = iRichSkill.statement
    this.keywords = iRichSkill.keywords
    this.publishStatus = iRichSkill.publishStatus
    this.category = iRichSkill.category
  }
}
