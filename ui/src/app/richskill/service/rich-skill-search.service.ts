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

  advancedMatchingQuery(): string[] {
    return Object.getOwnPropertyNames(this.advanced).flatMap((k) => {
      const a: any = this.advanced
      return a !== undefined ? a[k] : undefined
    }).filter(x => x !== undefined).map(it => it?.name ?? it).filter(it => (it?.length ?? 0) > 0)
  }
}

export class ApiAdvancedSearch {
  skillName?: string
  collectionName?: string
  category?: string
  skillStatement?: string
  keywords?: string[]
  occupations?: string[]
  standards?: INamedReference[]
  certifications?: INamedReference[]
  employers?: INamedReference[]
  alignments?: INamedReference[]
  author?: string

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
