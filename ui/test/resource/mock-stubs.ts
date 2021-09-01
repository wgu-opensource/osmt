import { Navigation } from "@angular/router"
import { Observable, of, Subject } from "rxjs"
import { ApiCollection, ICollectionUpdate } from "../../src/app/collection/ApiCollection"
import { PublishStatus } from "../../src/app/PublishStatus"
import { ApiBatchResult } from "../../src/app/richskill/ApiBatchResult"
import { ApiSkill, ApiSortOrder } from "../../src/app/richskill/ApiSkill"
import { ApiSkillSummary } from "../../src/app/richskill/ApiSkillSummary"
import { ApiSkillUpdate } from "../../src/app/richskill/ApiSkillUpdate"
import {
  ApiAdvancedSearch,
  ApiSearch,
  ApiSkillListUpdate,
  PaginatedCollections,
  PaginatedSkills
} from "../../src/app/richskill/service/rich-skill-search.service"
import { ApiTaskResult } from "../../src/app/task/ApiTaskResult"
import {
  createMockBatchResult,
  createMockCollection,
  createMockPaginatedCollections,
  createMockPaginatedSkills,
  createMockSkill,
  createMockSkillSummary,
  createMockTaskResult
} from "./mock-data"


// Add service stubs here.
// For more examples, see https://github.com/WGU-edu/ema-eval-ui/blob/develop/src/app/admin/pages/edit-user/edit-user.component.spec.ts


export class EnvironmentServiceStub {
  environment = {
    env: "local"
  }
}

export let RouterData = { commands: [] as string[], extras: {} }
export class RouterStub {
  // this.router.navigate(["/skills/search"], {queryParams: {q: query}})
  public navigate(commands: string[], extras: object): Promise<boolean> {
    RouterData.commands = commands
    RouterData.extras = extras
    return new Promise((resolve, reject) => { })
  }

  getCurrentNavigation(): Navigation | null {
    return null
  }
}

export let AuthServiceData = { isDown: false }
export class AuthServiceStub {  // TODO consider using real class
  public logout(): void {
  }
  public setServerIsDown(isDown: boolean): void {
    AuthServiceData.isDown = isDown
  }
  public currentAuthToken(): string | null {
    return "fake-token"
  }
}

export let SearchServiceData = {
  latestSearch: new ApiSearch({}) as ApiSearch | undefined,
  searchQuerySource: new Subject<ApiSearch>(),
  _latestQuery: ""
}
export class SearchServiceStub {
  public searchQuery$ = SearchServiceData.searchQuerySource.asObservable()

  simpleSkillSearch(query: string): void {
    SearchServiceData._latestQuery = query
  }
  advancedSkillSearch(advanced: ApiAdvancedSearch): void {
  }

  simpleCollectionSearch(query: string): void {
    SearchServiceData._latestQuery = query
  }
  advancedCollectionSearch(advanced: ApiAdvancedSearch): void {
  }

  setLatestSearch(apiSearch?: ApiSearch): void {
    SearchServiceData.latestSearch = apiSearch
    SearchServiceData.searchQuerySource.next(SearchServiceData.latestSearch)
  }

  public clearSearch(): void {
    SearchServiceData.latestSearch = undefined
    SearchServiceData.searchQuerySource.next(SearchServiceData.latestSearch)
  }
}

export let CollectionServiceData = {
  uuid: "uuid",
  apiSearch: new ApiSearch({}) as ApiSearch | undefined,
}
export class CollectionServiceStub {
  public publishCollectionsWithResult(
    apiSearch: ApiSearch,
    newStatus: PublishStatus = PublishStatus.Published,
    filterByStatuses?: Set<PublishStatus>,
    pollIntervalMs: number = 1000,
  ): Observable<ApiBatchResult> {
    return of(new ApiBatchResult({
      success: true,
      message: "yup!",
      totalCount: 42,
      modifiedCount: 7
    }))
  }

  getCollectionByUUID(uuid: string): Observable<ApiCollection> {
    return of(new ApiCollection(createMockCollection(
      new Date("2020-06-25T14:58:46.313Z"),
      new Date("2020-06-25T14:58:46.313Z"),
      new Date("2020-06-25T14:58:46.313Z"),
      new Date("2020-06-25T14:58:46.313Z"),
      PublishStatus.Draft
    )))
  }

  getCollections(
    size: number = 50,
    from: number = 0,
    filterByStatuses: Set<PublishStatus> | undefined,
    sort: ApiSortOrder | undefined,
  ): Observable<PaginatedCollections> {
    return of(createMockPaginatedCollections())
  }

  getCollectionSkills(
    collectionUuid: string,
    size?: number,
    from?: number,
    filterByStatuses?: Set<PublishStatus>,
    sort?: ApiSortOrder,
    apiSearch?: ApiSearch
  ): Observable<PaginatedSkills> {
    return of(createMockPaginatedSkills())
  }

  /** @param collectionUuid "uuid1" means not ready to publish.  All others are ready */
  collectionReadyToPublish(collectionUuid: string): Observable<boolean> {
    return of(collectionUuid !== "uuid1")
  }

  updateSkills(collectionUuid: string,
               skillListUpdate: ApiSkillListUpdate,
               filterByStatuses?: Set<PublishStatus>
  ): Observable<ApiTaskResult> {
    return of(new ApiTaskResult(createMockTaskResult()))
  }

  updateCollection(uuid: string, updateObject: ICollectionUpdate): Observable<ApiCollection> {
    return of(
      new ApiCollection(createMockCollection(
        new Date("2020-06-25T14:58:46.313Z"),
        new Date("2020-06-25T14:58:46.313Z"),
        new Date("2020-06-25T14:58:46.313Z"),
        new Date("2020-06-25T14:58:46.313Z"),
        PublishStatus.Draft
        // The default is to have some skills
      ))
    )
  }

  updateSkillsWithResult(collectionUuid: string,
                         skillListUpdate: ApiSkillListUpdate,
                         filterByStatus?: Set<PublishStatus>,
                         pollIntervalMs: number = 1000
  ): Observable<ApiBatchResult> {
    CollectionServiceData.uuid = collectionUuid
    CollectionServiceData.apiSearch = skillListUpdate.remove
    return of(new ApiBatchResult({
      success: true,
      message: "yup!",
      totalCount: 42,
      modifiedCount: 7
    }))
  }

  searchCollections(
    apiSearch: ApiSearch,
    size: number | undefined,
    from: number | undefined,
    filterByStatuses?: Set<PublishStatus>,
    sort?: ApiSortOrder,
  ): Observable<PaginatedCollections> {
    return of(createMockPaginatedCollections())
  }
}

export let RichSkillServiceData = {
}
export class RichSkillServiceStub {
  publishSkillsWithResult(
    apiSearch: ApiSearch,
    newStatus: PublishStatus = PublishStatus.Published,
    filterByStatuses?: Set<PublishStatus>,
    collectionUuid?: string,
    pollIntervalMs: number = 1000,
  ): Observable<ApiBatchResult> {
    return of(createMockBatchResult())
  }

  getSkillByUUID(uuid: string): Observable<ApiSkill> {
    const date = new Date("2020-06-25T14:58:46.313Z")
    return of(new ApiSkill(createMockSkill(date, date, PublishStatus.Draft)))
  }

  createSkill(updateObject: ApiSkillUpdate, pollIntervalMs: number = 1000): Observable<ApiSkill> {
    const date = new Date("2020-06-25T14:58:46.313Z")
    return of(new ApiSkill(createMockSkill(date, date, PublishStatus.Draft)))
  }

  createSkills(updateObjects: ApiSkillUpdate[]): Observable<ApiTaskResult> {
    return of(new ApiTaskResult(createMockTaskResult()))
  }

  updateSkill(uuid: string, updateObject: ApiSkillUpdate): Observable<ApiSkill> {
    const now = new Date()
    return of(new ApiSkill(createMockSkill(now, now, PublishStatus.Draft)))
  }

  similarityCheck(statement: string): Observable<ApiSkillSummary[]> {
    const isoDate = new Date().toISOString()
    return of([ new ApiSkillSummary(createMockSkillSummary("id1", PublishStatus.Draft, isoDate)) ])
  }
  similaritiesCheck(statement: string): Observable<boolean[]> {
    const isoDate = new Date().toISOString()
    return of([ true ])
  }

  pollForTaskResult<T>(obs: Observable<ApiTaskResult>, pollIntervalMs: number = 1000): Observable<T> {
    return new Observable<T>()
  }

  searchSkills(
    apiSearch: ApiSearch,
    size?: number,
    from?: number,
    filterByStatuses?: Set<PublishStatus>,
    sort?: ApiSortOrder,
  ): Observable<PaginatedSkills> {
    return of(createMockPaginatedSkills())
  }
}
