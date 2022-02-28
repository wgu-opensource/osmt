import {ApiSkillSummary} from "../../../richskill/ApiSkillSummary";

export interface ISkillSearchResult {
  skill: ApiSkillSummary
  similarToLocalSkill?: boolean
}

export class ApiSkillSearchResult implements ISkillSearchResult {
  skill: ApiSkillSummary
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
