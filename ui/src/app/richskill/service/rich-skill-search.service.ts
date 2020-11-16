import {Injectable} from "@angular/core"
import {AbstractService} from "../../abstract.service"
import {AuthService} from "../../auth/auth-service"
import {HttpClient} from "@angular/common/http"
import {Observable} from "rxjs"
import {ApiSkill, ISkill} from "../ApiSkill"
import {map} from "rxjs/operators"
import {ApiCollectionSummary, IApiSkillSummary, ICollectionSummary} from "../ApiSkillSummary"

export interface ISearch {
  query?: string
  advanced?: IAdvancedSearch
  uuids?: []
}

export interface IAdvancedSearch {
  skillName?: string
  collectionName?: string
  category?: string
  skillStatement?: string
  keywords?: []
  occupations?: []
  standards?: []
  certifications?: []
  employers?: []
  alignments?: []
  author?: string
}

export class ApiSearch implements ISearch {
  query?: string
  advanced?: IAdvancedSearch
  uuids?: []

  static factory(options: object): ApiSearch {
    return Object.assign(new ApiSearch(), options)
  }
}

export class ApiAdvancedSearch implements IAdvancedSearch {
  skillName: string | undefined
  collectionName: string | undefined
  category: string | undefined
  skillStatement: string | undefined
  keywords: [] | undefined
  occupations: [] | undefined
  standards: []| undefined
  certifications: [] | undefined
  employers: [] | undefined
  alignments: [] | undefined
  author: string | undefined // TODO Doesn't exist yet in api

  static factory(options: object): ApiAdvancedSearch {
    return Object.assign(new ApiAdvancedSearch(), options)
  }
}

export interface ISkillListUpdate {
  add?: ApiSearch
  remove?: string[]
}
export class ApiSkillListUpdate implements ISkillListUpdate {
  add?: ApiSearch
  remove?: string[]

  constructor({add, remove}: ISkillListUpdate) {
    this.add = add
    this.remove = remove
  }

}

export class PaginatedSkills {
  totalCount = 0
  skills: IApiSkillSummary[] = []
  constructor(skills: IApiSkillSummary[], totalCount: number) {
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
        console.log(body)
        return this.safeUnwrapBody(body, errorMsg).map(s => new ApiSkill(s))
      }))
  }
}
