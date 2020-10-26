import {Injectable} from "@angular/core"
import {AbstractService} from "../../abstract.service"
import {AuthService} from "../../auth/auth-service"
import {HttpClient} from "@angular/common/http"
import {Observable} from "rxjs"
import {ApiSkill, ISkill} from "../ApiSkill"
import {map} from "rxjs/operators"

export interface ISearch {
  query: string | undefined
  advanced: ApiAdvancedSearch | undefined
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
}

export class ApiSearch {
  query: string | undefined
  advanced: ApiAdvancedSearch | undefined
  uuids: [] | undefined

  static factory(options: object): ApiSearch {
    return Object.assign(new ApiSearch(), options)
  }
}

export class ApiAdvancedSearch {
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

  static factory(options: object): ApiSearch {
    return Object.assign(new ApiSearch(), options)
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
