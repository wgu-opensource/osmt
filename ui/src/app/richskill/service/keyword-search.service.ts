import {Injectable} from "@angular/core"
import {AbstractService} from "../../abstract.service"
import {HttpClient} from "@angular/common/http"
import {AuthService} from "../../auth/auth-service"
import {Observable} from "rxjs"
import {ApiNamedReference, INamedReference, KeywordType} from "../ApiSkill"
import {map} from "rxjs/operators"
import { ApiJobCode, IJobCode } from "../../metadata/job-codes/Jobcode"
import {Router} from "@angular/router";
import {Location} from "@angular/common";

/**
 * @deprecated
 */
@Injectable({
  providedIn: "root"
})
export class KeywordSearchService extends AbstractService {

  constructor(httpClient: HttpClient, authService: AuthService, router: Router, location: Location) {
    super(httpClient, authService, router, location)
  }

  searchJobcodes(
    query: string
  ): Observable<ApiJobCode[]> {
    const errorMsg = `Failed to unwrap response for skill search`

    return this.get<IJobCode[]>({
      path: "api/search/jobcodes",
      params: {
        query
      }
    })
      .pipe(map(({body}) => {
        return this.safeUnwrapBody(body, errorMsg).map(it => new ApiJobCode(it))
      }))
  }

  searchKeywords(
    type: KeywordType,
    query: string
  ): Observable<ApiNamedReference[]> {
    const errorMsg = `Failed to unwrap response for skill search`

    return this.get<INamedReference[]>({
      path: "api/search/keywords",
      params: {
        query,
        type
      }
    })
      .pipe(map(({body}) => {
        return this.safeUnwrapBody(body, errorMsg).map(it => new ApiNamedReference(it))
      }))
  }
}
