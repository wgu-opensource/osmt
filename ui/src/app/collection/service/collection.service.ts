import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {AuthService} from "../../auth/auth-service";
import {AbstractService} from "../../abstract.service";
import {PublishStatus} from "../../PublishStatus";
import {ApiSkillSortOrder} from "../../richskill/ApiSkill";
import {ApiSearch, PaginatedCollections, PaginatedSkills} from "../../richskill/service/rich-skill-search.service";
import {Observable} from "rxjs";
import {ApiCollectionSummary, ApiSkillSummary, ICollectionSummary} from "../../richskill/ApiSkillSummary";
import {map, share} from "rxjs/operators";
import {ApiBatchResult} from "../../richskill/ApiBatchResult";
import {ApiTaskResult, ITaskResult} from "../../task/ApiTaskResult";

@Injectable({
  providedIn: "root"
})
export class CollectionService extends AbstractService {

  constructor(httpClient: HttpClient, authService: AuthService) {
    super(httpClient, authService)
  }

  searchCollections(
    apiSearch: ApiSearch,
    size: number | undefined,
    from: number | undefined,
    filterByStatuses?: Set<PublishStatus>,
    sort?: ApiSkillSortOrder,
  ): Observable<PaginatedCollections> {

    const params = this.buildTableParams(size, from, filterByStatuses, sort)

    return this.post<ICollectionSummary[]>({
      path: "api/search/collections",
      params,
      body: apiSearch
    })
      .pipe(share())
      .pipe(map(({body, headers}) => {
        const totalCount = Number(headers.get("X-Total-Count"))
        const collections = body?.map(it => new ApiCollectionSummary(it)) || []
        return new PaginatedCollections(collections, !isNaN(totalCount) ? totalCount : collections.length)
      }))
  }

  addSkillsToCollection(collectionUuid: string, apiSearch: ApiSearch): Observable<ApiTaskResult> {
    return this.post<ITaskResult>({
      path: `api/collections/${collectionUuid}/skills`,
      body: apiSearch
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiTaskResult(this.safeUnwrapBody(body, "unwrap failure"))))
  }

  addSkillsWithResult(collectionUuid: string, apiSearch: ApiSearch, pollIntervalMs: number = 1000): Observable<ApiBatchResult> {
    return this.pollForTaskResult(this.addSkillsToCollection(collectionUuid, apiSearch))
  }
}
