import {Injectable} from "@angular/core"
import {AbstractService} from "../../abstract.service"
import {AuthService} from "../../auth/auth-service"
import {HttpClient} from "@angular/common/http"
import {Observable} from "rxjs"
import {ApiSkill, ISkill} from "../ApiSkill"
import {map} from "rxjs/operators"
import {IApiSkillSummary} from "../ApiSkillSummary"

export interface ISearch {
  query: string | undefined
  advanced: IAdvancedSearch | undefined
  uuids: [] | undefined
}

export interface IAdvancedSearch {
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
  author: string | undefined
}

export class ApiSearch implements ISearch {
  query: string | undefined
  advanced: IAdvancedSearch | undefined
  uuids: [] | undefined

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
  author: string | undefined // TODO there's some white label shenanigans here

  static factory(options: object): ApiAdvancedSearch {
    return Object.assign(new ApiAdvancedSearch(), options)
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
