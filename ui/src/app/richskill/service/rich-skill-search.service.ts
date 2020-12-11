import {INamedReference} from "../ApiSkill"
import {ApiCollectionSummary, ApiSkillSummary} from "../ApiSkillSummary"

export interface ISearch {
  query?: string
  advanced?: ApiAdvancedSearch
  uuids?: string[]
}

export class ApiSearch implements ISearch {
  query?: string
  advanced?: ApiAdvancedSearch
  uuids?: string[]

  constructor({query, advanced, uuids}: ISearch) {
    this.query = query
    this.advanced = advanced
    this.uuids = uuids
  }

}

export class ApiAdvancedSearch {
  skillName?: string
  collectionName?: string
  category?: string
  skillStatement?: string
  keywords?: string[]
  occupations?: INamedReference[]
  standards?: INamedReference[]
  certifications?: INamedReference[]
  employers?: INamedReference[]
  alignments?: INamedReference[]
  author?: INamedReference

  static factory(options: ApiAdvancedSearch): ApiAdvancedSearch {
    return Object.assign(new ApiAdvancedSearch(), options)
  }
}

export interface ISkillListUpdate {
  add?: ApiSearch
  remove?: ApiSearch
}
export class ApiSkillListUpdate implements ISkillListUpdate {
  add?: ApiSearch
  remove?: ApiSearch

  constructor({add, remove}: ISkillListUpdate) {
    this.add = add
    this.remove = remove
  }

}

export class PaginatedSkills {
  totalCount = 0
  skills: ApiSkillSummary[] = []
  constructor(skills: ApiSkillSummary[], totalCount: number) {
    this.skills = skills
    this.totalCount = totalCount
  }
}

export class PaginatedCollections {
  totalCount = 0
  collections: ApiCollectionSummary[] = []
  constructor(collections: ApiCollectionSummary[], totalCount: number) {
    this.collections = collections
    this.totalCount = totalCount
  }
}
