import {KeywordType} from "../richskill/ApiSkill"

export interface IKeyword {
  type: KeywordType
  id: number
  value: string
  skillCount: number
}

export enum KeywordSortOrder {
  KeywordAsc = "keyword.asc",
  KeywordDesc = "keyword.desc",
  SkillCountAsc = "skillCount.asc",
  SkillCountDesc = "skillCount.desc"
}

export class ApiCategory {
  id: number
  name: string
  skillCount: number

  constructor(keyword: IKeyword) {
    this.id = keyword.id
    this.name = keyword.value
    this.skillCount = keyword.skillCount
  }
}

export class PaginatedCategories {
  categories: ApiCategory[] = []
  totalCount = 0

  constructor(keywords: IKeyword[], totalCount: number) {
    this.categories = keywords.map(kw => new ApiCategory(kw))
    this.totalCount = totalCount
  }
}
