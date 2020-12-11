import {Injectable} from "@angular/core";
import {AbstractService} from "../../abstract.service";
import {HttpClient} from "@angular/common/http";
import {AuthService} from "../../auth/auth-service";
import {Observable} from "rxjs";
import {ApiNamedReference, ApiSkill, INamedReference, ISkill, KeywordType} from "../ApiSkill";
import {map} from "rxjs/operators";
import {ApiSearch} from "./rich-skill-search.service";
import {ApiJobCode, IJobCode} from "../../job-codes/Jobcode";

@Injectable({
  providedIn: "root"
})
export class KeywordSearchService extends AbstractService {

  constructor(httpClient: HttpClient, authService: AuthService) {
    super(httpClient, authService)
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
