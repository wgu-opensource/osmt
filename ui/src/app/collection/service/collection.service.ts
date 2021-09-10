import {Injectable} from "@angular/core"
import {HttpClient, HttpHeaders} from "@angular/common/http"
import {AuthService} from "../../auth/auth-service"
import {AbstractService} from "../../abstract.service"
import {PublishStatus} from "../../PublishStatus"
import {ApiAuditLog, ApiSortOrder, IAuditLog} from "../../richskill/ApiSkill"
import {
  ApiSearch,
  ApiSkillListUpdate,
  PaginatedCollections,
  PaginatedSkills
} from "../../richskill/service/rich-skill-search.service"
import {Observable} from "rxjs"
import {ApiCollectionSummary, ApiSkillSummary, ICollectionSummary} from "../../richskill/ApiSkillSummary"
import {map, share} from "rxjs/operators"
import {ApiBatchResult} from "../../richskill/ApiBatchResult"
import {ApiTaskResult, ITaskResult} from "../../task/ApiTaskResult"
import {ApiCollection, ICollection, ICollectionUpdate} from "../ApiCollection"
import {Router} from "@angular/router";
import {Location} from "@angular/common";

@Injectable({
  providedIn: "root"
})
export class CollectionService extends AbstractService {

  private baseServiceUrl = "api/collections"

  constructor(httpClient: HttpClient, authService: AuthService, router: Router, location: Location) {
    super(httpClient, authService, router, location)
  }

  getCollections(
    size: number = 50,
    from: number = 0,
    filterByStatuses: Set<PublishStatus> | undefined,
    sort: ApiSortOrder | undefined,
  ): Observable<PaginatedCollections> {
    const params = this.buildTableParams(size, from, filterByStatuses, sort)
    return this.get<ICollectionSummary[]>({
      path: `${this.baseServiceUrl}`,
      params,
    })
      .pipe(share())
      .pipe(map(({body, headers}) => {
        return new PaginatedCollections(
          body?.map(skill => new ApiCollectionSummary(skill)) || [],
          Number(headers.get("X-Total-Count"))
        )
      }))
  }

  getCollectionByUUID(uuid: string): Observable<ApiCollection> {
    const errorMsg = `Could not find skill by uuid [${uuid}]`
    return this.get<ICollection>({
      path: `${this.baseServiceUrl}/${uuid}`
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiCollection(this.safeUnwrapBody(body, errorMsg))))
  }

  getCollectionJson(uuid: string): Observable<string> {
    if (!uuid) {
      throw new Error("No uuid provided for collection json export")
    }
    const errorMsg = `Could not find collection by uuid [${uuid}]`
    return this.httpClient
      .get(this.buildUrl(`${this.baseServiceUrl}/${uuid}`), {
        headers: this.wrapHeaders(new HttpHeaders({
          Accept: "application/json"
        })),
        responseType: "text",
        observe: "response"
      })
      .pipe(share())
      .pipe(map(({body}) => this.safeUnwrapBody(body, errorMsg)))
  }

  requestCollectionSkillsCsv(uuid: string): Observable<ITaskResult> {
    if (!uuid) {
      throw new Error("Invalid collection uuid.")
    }
    const errorMsg = `Could not find skills using this collection [${uuid}]`

    return this.get<ITaskResult>({
      path: `${this.baseServiceUrl}/${uuid}/csv`
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiTaskResult(this.safeUnwrapBody(body, errorMsg))))
  }

  // this call is a bit different since it's returning a csv for immediate download, so use httpClient's get() method
  // tslint:disable-next-line:no-any
  getCsvTaskResultsIfComplete(uuid: string): Observable<any> {
    return this.httpClient
      .get(this.buildUrl(`/api/results/text/${uuid}`), {
        responseType: "text",
        observe: "response"
      })
      .pipe(share())
  }


  getCollectionSkills(
    collectionUuid: string,
    size?: number,
    from?: number,
    filterByStatuses?: Set<PublishStatus>,
    sort?: ApiSortOrder,
    apiSearch?: ApiSearch): Observable<PaginatedSkills> {
    const errorMsg = `Could not find skills in collection [${collectionUuid}]`
    return this.post<ApiSkillSummary[]>({
      path: `${this.baseServiceUrl}/${collectionUuid}/skills`,
      headers: new HttpHeaders({
        Accept: "application/json"
      }),
      params: this.buildTableParams(size, from, filterByStatuses, sort),
      body: apiSearch ?? new ApiSearch({})
    })
      .pipe(share())
      .pipe(map(({body, headers}) =>
        new PaginatedSkills(this.safeUnwrapBody(body, errorMsg), Number(headers.get("X-Total-Count")))
      ))
  }

  createCollection(updateObject: ICollectionUpdate): Observable<ApiCollection> {
    const errorMsg = `Error creating collection`
    return this.post<ApiCollection[]>({
      path: this.baseServiceUrl,
      body: [updateObject]
    })
      .pipe(share())
      .pipe(map(({body}) => this.safeUnwrapBody(body, errorMsg).map(s => new ApiCollection(s))[0]))
  }

  updateCollection(uuid: string, updateObject: ICollectionUpdate): Observable<ApiCollection> {
    const errorMsg = `Could not find collection by uuid [${uuid}]`
    return this.post<ICollection>({
      path: `${this.baseServiceUrl}/${uuid}/update`,
      body: updateObject
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiCollection(this.safeUnwrapBody(body, errorMsg))))
  }

  searchCollections(
    apiSearch: ApiSearch,
    size: number | undefined,
    from: number | undefined,
    filterByStatuses?: Set<PublishStatus>,
    sort?: ApiSortOrder,
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

  updateSkills(collectionUuid: string,
               skillListUpdate: ApiSkillListUpdate,
               filterByStatuses?: Set<PublishStatus>
  ): Observable<ApiTaskResult> {
    const params = this.buildTableParams(undefined, undefined, filterByStatuses, undefined)
    return this.post<ITaskResult>({
      path: `api/collections/${collectionUuid}/updateSkills`,
      params,
      body: skillListUpdate
    })
      .pipe(share())
      .pipe(map(({body}) => new ApiTaskResult(this.safeUnwrapBody(body, "unwrap failure"))))
  }

  updateSkillsWithResult(collectionUuid: string,
                         skillListUpdate: ApiSkillListUpdate,
                         filterByStatus?: Set<PublishStatus>,
                         pollIntervalMs: number = 1000
  ): Observable<ApiBatchResult> {
    return this.pollForTaskResult<ApiBatchResult>(this.updateSkills(collectionUuid, skillListUpdate, filterByStatus), pollIntervalMs)
  }

  publishCollectionsWithResult(
    apiSearch: ApiSearch,
    newStatus: PublishStatus = PublishStatus.Published,
    filterByStatuses?: Set<PublishStatus>,
    pollIntervalMs: number = 1000,
  ): Observable<ApiBatchResult> {
    return this.pollForTaskResult<ApiBatchResult>(
      this.bulkStatusChange("api/collections/publish", apiSearch, newStatus, filterByStatuses),
      pollIntervalMs
    )
  }

  collectionReadyToPublish(collectionUuid: string): Observable<boolean> {
    return new Observable(observer => {
      const filters = new Set([PublishStatus.Archived, PublishStatus.Draft])
      this.getCollectionSkills(collectionUuid, 1, 0, filters).subscribe(results => {
        const isReady = results.totalCount === 0
        observer.next(isReady)
        observer.complete()
      })
    })
  }

  auditLog(
    uuid?: string
  ): Observable<ApiAuditLog[]> {
    return this.get<IAuditLog[]>({
      path: `${this.baseServiceUrl}/${uuid}/log`
    })
      .pipe(share())
      .pipe(map(({body, headers}) => {
        return body?.map(it => new ApiAuditLog(it)) || []
      }))
  }
}
