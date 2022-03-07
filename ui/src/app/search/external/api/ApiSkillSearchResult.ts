import {ISkillSummary} from "../../../richskill/ApiSkillSummary";

export interface ISkillSearchResult {
  skill: ISkillSummary
  similarToLocalSkill?: boolean
}

export class ApiSkillSearchResult implements ISkillSearchResult {
  skill: ISkillSummary
  similarToLocalSkill?: boolean

  constructor(skillSearchResult: ISkillSearchResult) {
    this.skill = skillSearchResult.skill
    this.similarToLocalSkill = skillSearchResult.similarToLocalSkill
  }
}

export class PaginatedSkillSearchResults {
  totalCount = 0
  results: ISkillSearchResult[] = []

  constructor(results: ISkillSearchResult[], totalCount: number) {
    this.results = results
    this.totalCount = totalCount
  }
}
