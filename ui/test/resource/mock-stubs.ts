import { EventEmitter } from "@angular/core"
import { Navigation } from "@angular/router"
import { Observable, of, Subject } from "rxjs"
import { ApiCollection, ICollectionUpdate } from "../../src/app/collection/ApiCollection"
import { ApiJobCode } from "../../src/app/job-codes/Jobcode"
import { PublishStatus } from "../../src/app/PublishStatus"
import { ApiBatchResult } from "../../src/app/richskill/ApiBatchResult"
import { ApiNamedReference, ApiSkill, ApiSortOrder, KeywordType } from "../../src/app/richskill/ApiSkill"
import { ApiSkillSummary } from "../../src/app/richskill/ApiSkillSummary"
import { ApiSkillUpdate } from "../../src/app/richskill/ApiSkillUpdate"
import {
  ApiAdvancedSearch,
  ApiSearch,
  ApiSkillListUpdate,
  PaginatedCollections,
  PaginatedSkills
} from "../../src/app/richskill/service/rich-skill-search.service"
import { ApiTaskResult, ITaskResult } from "../../src/app/task/ApiTaskResult"
import {ButtonAction, OSMT_ADMIN} from "../../src/app/auth/auth-roles"
import {
  apiTaskResultForDeleteCollection,
  createMockBatchResult,
  createMockCollection,
  createMockJobcode,
  createMockPaginatedCollections,
  createMockPaginatedSkills,
  createMockSkill,
  createMockSkillSummary,
  createMockTaskResult,
  mockTaskResultForExportSearch
} from "./mock-data"
import {ApiCategory, CategoryService, IKeyword, KeywordSortOrder, PaginatedCategories} from "../../src/app/category";
import {IRelatedSkillsService} from "../../src/app/abstract.service";


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
    // noinspection JSUnusedLocalSymbols
    return new Promise((resolve, reject) => { })
  }

  // noinspection JSUnusedGlobalSymbols
  getCurrentNavigation(): Navigation | null {
    return null
  }
}

export let AuthServiceData = {
  isDown: false,
  authenticatedFlag: true,
  hasRoleFlag: true
}
export class AuthServiceStub {  // TODO consider using real class
  init(): void {
  }
  setup(): void {
  }
  start(returnPath: string): void {
  }
  public logout(): void {
  }
  // noinspection JSUnusedGlobalSymbols
  public setServerIsDown(isDown: boolean): void {
    AuthServiceData.isDown = isDown
  }
  public currentAuthToken(): string | null {
    return "fake-token"
  }
  public getRole(): string | null {
    return OSMT_ADMIN
  }
  public isAuthenticated(): boolean {
    return AuthServiceData.authenticatedFlag
  }
  hasRole(requiredRoles: string[], userRoles: string[]): boolean {
    return AuthServiceData.hasRoleFlag
  }
  isEnabledByRoles(buttonAction : ButtonAction): boolean {
    return true
  }
}

export let SearchServiceData = {
  latestSearch: new ApiSearch({}) as ApiSearch | undefined,
  searchQuerySource: new Subject<ApiSearch>(),
  _latestQuery: ""
}
export class SearchServiceStub {
  // noinspection JSUnusedGlobalSymbols - used elsewhere
  public searchQuery$ = SearchServiceData.searchQuerySource.asObservable()

  // noinspection JSUnusedGlobalSymbols
  simpleSkillSearch(query: string): void {
    SearchServiceData._latestQuery = query
  }
  // noinspection JSUnusedLocalSymbols
  // tslint:disable-next-line:variable-name
  advancedSkillSearch(_advanced: ApiAdvancedSearch): void {
    const advanced = _advanced as ApiAdvancedSearch
    SearchServiceData.latestSearch = new ApiSearch({ advanced, filtered: {} })
    this.setLatestSearch(new ApiSearch({advanced, filtered: {}}))
  }

  simpleCollectionSearch(query: string): void {
    SearchServiceData._latestQuery = query
  }
  // noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
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

export class RelatedSkillServiceStub implements IRelatedSkillsService<number> {
  getRelatedSkills(
    entityId: number,
    size: number,
    from: number,
    statusFilters: Set<PublishStatus>,
    sort?: ApiSortOrder
  ): Observable<PaginatedSkills> {
    return of(createMockPaginatedSkills())
  }

  searchRelatedSkills(
    entityId: number,
    size: number,
    from: number,
    statusFilters: Set<PublishStatus>,
    sort?: ApiSortOrder,
    search?: ApiSearch
  ): Observable<PaginatedSkills>  {
    return of(createMockPaginatedSkills())
  }
}

const testCategoryKeyword= {
  type: KeywordType.Category,
  id: 1,
  value: "category-1",
  skillCount: 20
}
export let CategoryServiceData = {
  category: new ApiCategory(testCategoryKeyword),
  categoryKeyword: testCategoryKeyword,
  paginatedCategories: new PaginatedCategories([testCategoryKeyword], 1),
  paginatedSkills: createMockPaginatedSkills()
}
export class CategoryServiceStub {
  getAllPaginated(
    size: number = 50,
    from: number = 0,
    sort: KeywordSortOrder | undefined,
  ): Observable<PaginatedCategories> {
    return of(CategoryServiceData.paginatedCategories)
  }

  getById(identifier: string): Observable<ApiCategory> {
    return of(CategoryServiceData.category)
  }

  getRelatedSkills(
    entityId: number,
    size: number,
    from: number,
    statusFilters: Set<PublishStatus>,
    sort?: ApiSortOrder,
  ): Observable<PaginatedSkills> {
    return of(CategoryServiceData.paginatedSkills)
  }

  searchRelatedSkills(
    entityId: number,
    size: number,
    from: number,
    statusFilters: Set<PublishStatus>,
    sort?: ApiSortOrder,
    apiSearch?: ApiSearch
  ): Observable<PaginatedSkills> {
    return of(CategoryServiceData.paginatedSkills)
  }
}

export let CollectionServiceData = {
  uuid: "uuid",
  apiSearch: new ApiSearch({}) as ApiSearch | undefined,
}
export class CollectionServiceStub {
  // noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
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

  // noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  getCollectionByUUID(uuid: string): Observable<ApiCollection> {
    const date = new Date("2020-06-25T14:58:46.313Z")
    return of(new ApiCollection(createMockCollection(
      date,
      date,
      date,
      date,
      PublishStatus.Draft
    )))
  }

  getCollectionJson(uuid: string): Observable<string> {
    const date = new Date("2020-06-25T14:58:46.313Z")
    return of(JSON.stringify(new ApiCollection(createMockCollection(
      date,
      date,
      date,
      date,
      PublishStatus.Draft
    ))))
  }

  // noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols,MagicNumberJS
  getCollections(
    size: number = 50,
    from: number = 0,
    filterByStatuses: Set<PublishStatus> | undefined,
    sort: ApiSortOrder | undefined,
  ): Observable<PaginatedCollections> {
    return of(createMockPaginatedCollections())
  }

  // noinspection OverlyComplexFunctionJS,JSUnusedLocalSymbols
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

  // tslint:disable-next-line:no-any
  getCsvTaskResultsIfComplete(uuid: string): Observable<any> {
    return of({
      body: createMockTaskResult(uuid),
      status: 200
    })
  }

  getXlsxTaskResultsIfComplete(uuid: string): Observable<any> {
    return of({
      body: createMockTaskResult(uuid),
      status: 200
    })
  }

  requestCollectionSkillsCsv(uuid: string): Observable<ITaskResult> {
    return of(createMockTaskResult())
  }

  requestCollectionSkillsXlsx(uuid: string): Observable<ITaskResult> {
    return of(createMockTaskResult())
  }

  // noinspection JSUnusedLocalSymbols
  updateSkills(collectionUuid: string,
               skillListUpdate: ApiSkillListUpdate,
               filterByStatuses?: Set<PublishStatus>
  ): Observable<ApiTaskResult> {
    return of(new ApiTaskResult(createMockTaskResult()))
  }

  // noinspection JSUnusedLocalSymbols
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

  // noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
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

  // noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  searchCollections(
    apiSearch: ApiSearch,
    size: number | undefined,
    from: number | undefined,
    filterByStatuses?: Set<PublishStatus>,
    sort?: ApiSortOrder,
  ): Observable<PaginatedCollections> {
    return of(createMockPaginatedCollections())
  }

  getWorkspace(): Observable<ApiCollection> {
    const date = new Date()
    return of(createMockCollection(
      date,
      date,
      undefined,
      undefined,
      PublishStatus.Workspace
    ))
  }

  deleteCollectionWithResult(uuid: string): Observable<ApiTaskResult> {
    return of(apiTaskResultForDeleteCollection)
  }

  deleteCollection(uuid: string): Observable<ApiTaskResult> {
    return of(apiTaskResultForDeleteCollection)
  }

  createCollection(updateObject: ICollectionUpdate): Observable<ApiCollection> {
    const date = new Date("2020-06-25T14:58:46.313Z")
    return of(new ApiCollection(createMockCollection(
      date,
      date,
      date,
      date,
      PublishStatus.Draft
    )))
  }
}

// noinspection JSUnusedGlobalSymbols
export let RichSkillServiceData = {
}
export class RichSkillServiceStub {
  // noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  publishSkillsWithResult(
    apiSearch: ApiSearch,
    newStatus: PublishStatus = PublishStatus.Published,
    filterByStatuses?: Set<PublishStatus>,
    collectionUuid?: string,
    pollIntervalMs: number = 1000,
  ): Observable<ApiBatchResult> {
    return of(createMockBatchResult())
  }

  // noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  getSkillByUUID(uuid: string): Observable<ApiSkill> {
    const date = new Date("2020-06-25T14:58:46.313Z")
    return of(new ApiSkill(createMockSkill(date, date, PublishStatus.Draft)))
  }

  // noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  getSkillJsonByUuid(uuid: string): Observable<string> {
    const date = new Date("2020-06-25T14:58:46.313Z")
    return of(JSON.stringify(createMockSkill(date, date, PublishStatus.Published)))
  }

  getSkillCsvByUuid(uuid: string): Observable<string> {
    const date = new Date("2020-06-25T14:58:46.313Z")
    return of(`x, y, z`)
  }

  getSkillXlsxByUuid(uuid: string): Observable<string> {
    const date = new Date("2020-06-25T14:58:46.313Z")
    return of(`x, y, z`)
  }

  // noinspection JSUnusedLocalSymbols
  createSkill(updateObject: ApiSkillUpdate, pollIntervalMs: number = 1000): Observable<ApiSkill> {
    const date = new Date("2020-06-25T14:58:46.313Z")
    return of(new ApiSkill(createMockSkill(date, date, PublishStatus.Draft)))
  }

  // noinspection JSUnusedLocalSymbols
  createSkills(updateObjects: ApiSkillUpdate[]): Observable<ApiTaskResult> {
    return of(new ApiTaskResult(createMockTaskResult()))
  }

  // noinspection JSUnusedLocalSymbols
  updateSkill(uuid: string, updateObject: ApiSkillUpdate): Observable<ApiSkill> {
    const now = new Date()
    return of(new ApiSkill(createMockSkill(now, now, PublishStatus.Draft)))
  }

  similaritiesResults(statements: string[]): Observable<Array<ApiSkillSummary[]>> {
    return of([[]]);
  }

  // noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  similarityCheck(statement: string): Observable<ApiSkillSummary[]> {
    const isoDate = new Date().toISOString()
    return of([ new ApiSkillSummary(createMockSkillSummary("id1", PublishStatus.Draft, isoDate)) ])
  }
  // noinspection JSUnusedLocalSymbols
  similaritiesCheck(statement: string): Observable<boolean[]> {
    const isoDate = new Date().toISOString()
    return of([ true ])
  }

  // noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  pollForTaskResult<T>(obs: Observable<ApiTaskResult>, pollIntervalMs: number = 1000): Observable<T> {
    return new Observable<T>()
  }

  // noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  searchSkills(
    apiSearch: ApiSearch,
    size?: number,
    from?: number,
    filterByStatuses?: Set<PublishStatus>,
    sort?: ApiSortOrder,
  ): Observable<PaginatedSkills> {
    return of(createMockPaginatedSkills())
  }

  // noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  libraryExportCsv(): Observable<string> {
    return of(`x, y, z`)
  }

  // noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  libraryExportXlsx(): Observable<string> {
    return of(`x, y, z`)
  }

  exportSearchCsv(): Observable<ApiTaskResult> {
    return of(mockTaskResultForExportSearch)
  }

  exportSearchXlsx(): Observable<ApiTaskResult> {
    return of(mockTaskResultForExportSearch)
  }

  getResultExportedCsvLibrary(): Observable<any> {
    return of("")
  }

  getResultExportedXlsxLibrary(): Observable<any> {
    return of("")
  }

  getSkillsFiltered(
    size: number = 50,
    from: number = 0,
    apiSearch: ApiSearch,
    filterByStatuses: Set<PublishStatus> | undefined,
    sort: ApiSortOrder | undefined,
  ): Observable<PaginatedSkills> {
    return of(createMockPaginatedSkills())
  }
}

export let KeywordSearchServiceData = {
}
export class KeywordSearchServiceStub {
  searchJobcodes(query: string): Observable<ApiJobCode[]> {
    switch (query) {
      case "one":
        return of([ createMockJobcode(1, query, query) ])
    }
    return of([])
  }

  searchKeywords(type: KeywordType, query: string): Observable<ApiNamedReference[]> {
    return of([])
  }
}


// noinspection JSUnusedGlobalSymbols
export let IdleData = {
}
export class IdleStub {
  onTimeout: EventEmitter<number> = new EventEmitter<number>()

  // noinspection JSUnusedLocalSymbols
  setIdle(timeout: number): void {
  }
  // noinspection JSUnusedGlobalSymbols,JSUnusedLocalSymbols
  setTimeout(timeout: number): void {
  }
  // tslint:disable-next-line:no-any
  setInterrupts(interrupts: any[]): void {
  }
  watch(): void {
  }
}
