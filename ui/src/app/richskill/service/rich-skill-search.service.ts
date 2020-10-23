import {Injectable} from "@angular/core"
import {AbstractService} from "../../abstract.service"
import {AuthService} from "../../auth/auth-service"
import {HttpClient} from "@angular/common/http"
import {Observable} from "rxjs"
import {ApiSkill, ISkill} from "../ApiSkill"
import {map} from "rxjs/operators"

export interface ApiSearch {
  query: string | undefined
  advanced: Partial<ApiAdvancedSearch>
  uuids: [] | undefined
}

export interface ApiAdvancedSearch {
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
