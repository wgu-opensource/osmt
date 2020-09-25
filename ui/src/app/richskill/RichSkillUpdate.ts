import {IKeyword} from "../keyword/Keyword";

export interface IRichSkillUpdate {
  skillName?: string
  skillStatement?: string
}

export class RichSkillUpdate {
  skillName?: string
  skillStatement?: string

  constructor(iSkillUpdate: IRichSkillUpdate) {
    if (iSkillUpdate.skillName) this.skillName = iSkillUpdate.skillName
    if (iSkillUpdate.skillStatement) this.skillStatement = iSkillUpdate.skillStatement
  }
}
