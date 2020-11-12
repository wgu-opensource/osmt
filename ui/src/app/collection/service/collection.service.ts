import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {AuthService} from "../../auth/auth-service";
import {AbstractService} from "../../abstract.service";
import {PublishStatus} from "../../PublishStatus";
import {ApiSkillSortOrder} from "../../richskill/ApiSkill";
import {ApiSearch, ApiSkillListUpdate, PaginatedCollections} from "../../richskill/service/rich-skill-search.service";
import {Observable} from "rxjs";
import {ApiCollectionSummary, ICollectionSummary} from "../../richskill/ApiSkillSummary";
import {map, share} from "rxjs/operators";
import {ApiBatchResult} from "../../richskill/ApiBatchResult";
import {ApiTaskResult, ITaskResult} from "../../task/ApiTaskResult";
import {Collection, CollectionUpdate} from "../Collection"

@Injectable({
  providedIn: "root"
})
export class CollectionService extends AbstractService {

  private baseServiceUrl = "api/collections"

  constructor(httpClient: HttpClient, authService: AuthService) {
    super(httpClient, authService)
  }

  createCollection(collections: CollectionUpdate[]): Observable<Collection[]> {
    return this.post<Collection[]>({
      path: this.baseServiceUrl,
      body: collections
    })
      .pipe(share())
      .pipe(map(({body}) => this.safeUnwrapBody(body, "Failed to parse create collection response")))
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
      body: new ApiSkillListUpdate({add: apiSearch})
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiTaskResult(this.safeUnwrapBody(body, "unwrap failure"))))
  }

  addSkillsWithResult(collectionUuid: string, apiSearch: ApiSearch, pollIntervalMs: number = 1000): Observable<ApiBatchResult> {
    return this.pollForTaskResult(this.addSkillsToCollection(collectionUuid, apiSearch))
  }
}
