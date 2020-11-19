import {Injectable} from "@angular/core"
import {AbstractService} from "../../abstract.service"
import {AuthService} from "../../auth/auth-service"
import {HttpClient} from "@angular/common/http"
import {Observable} from "rxjs"
import {ApiSkill, INamedReference, ISkill} from "../ApiSkill"
import {map} from "rxjs/operators"
import {ApiCollectionSummary, ApiSkillSummary, ICollectionSummary} from "../ApiSkillSummary"

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
  author?: string  // TODO Doesn't exist yet in api

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

@Injectable({
  providedIn: "root"
})
export class RichSkillSearchService extends AbstractService {

  constructor(httpClient: HttpClient, authService: AuthService) {
    super(httpClient, authService)
  }

  searchSkills(
    size: number,
    from: number,
    status: string[],
    sort: string,
    searchBody: Partial<ApiSearch>
  ): Observable<ApiSkill[]> | null {
    const errorMsg = `Failed to unwrap response for skill search`

    return this.post<ISkill[]>({
      path: "api/search/skills",
      params: {
        size: size.toString(),
        from: from.toString(),
        status,
        sort
      },
      body: searchBody
    })
      .pipe(map(({body}) => {
        return this.safeUnwrapBody(body, errorMsg).map(s => new ApiSkill(s))
      }))
  }
}
