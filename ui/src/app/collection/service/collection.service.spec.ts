// noinspection LocalVariableNamingConventionJS

import { Location } from "@angular/common"
import { HttpClient, HttpResponse } from "@angular/common/http"
import { HttpClientTestingModule, HttpTestingController } from "@angular/common/http/testing"
import {async, fakeAsync, flush, TestBed, tick} from "@angular/core/testing"
import { Router } from "@angular/router"
import {
  apiTaskResultForDeleteCollection,
  createMockBatchResult,
  createMockCollection,
  createMockPaginatedCollections,
  createMockPaginatedSkills,
  createMockTaskResult,
  csvContent
} from "../../../../test/resource/mock-data"
import { AuthServiceData, AuthServiceStub, RouterData, RouterStub } from "../../../../test/resource/mock-stubs"
import { AppConfig } from "../../app.config"
import { AuthService } from "../../auth/auth-service"
import { EnvironmentService } from "../../core/environment.service"
import { PublishStatus } from "../../PublishStatus"
import { ApiBatchResult } from "../../richskill/ApiBatchResult"
import { ApiSortOrder } from "../../richskill/ApiSkill"
import { IStringListUpdate } from "../../richskill/ApiSkillUpdate"
import {
  ApiSearch,
  ApiSkillListUpdate,
  PaginatedCollections,
  PaginatedSkills
} from "../../richskill/service/rich-skill-search.service"
import { ApiTaskResult, ITaskResult } from "../../task/ApiTaskResult"
import { ApiCollection, ApiCollectionUpdate } from "../ApiCollection"
import { CollectionService } from "./collection.service"


const ASYNC_WAIT_PERIOD = 3000

describe("CollectionService", () => {
  let httpClient: HttpClient
  let httpTestingController: HttpTestingController
  let router: RouterStub
  let authService: AuthServiceStub
  let testService: CollectionService

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [],
      imports: [
        HttpClientTestingModule,
      ],
      providers: [
        EnvironmentService,
        AppConfig,
        CollectionService,
        Location,
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: Router, useClass: RouterStub }
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName

    httpClient = TestBed.inject(HttpClient)
    httpTestingController = TestBed.inject(HttpTestingController)
    router = TestBed.inject(Router)
    authService = TestBed.inject(AuthService)
    testService = TestBed.inject(CollectionService)
  }))

  afterEach(() => {
    httpTestingController.verify()
  })

  it("should be created", () => {
    expect(testService).toBeTruthy()
  })

  it("getCollections should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const path = "api/collections?sort=name.asc&status=draft&size=3&from=0"
    const testData: PaginatedCollections = createMockPaginatedCollections(3, 10)
    const statuses = new Set<PublishStatus>([ PublishStatus.Draft ])

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.getCollections(testData.collections.length, 0, statuses, ApiSortOrder.NameAsc)

    // Assert
    result$
      .subscribe((data: PaginatedCollections) => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("GET")
    req.flush(testData.collections, {
      headers: { "x-total-count": "" + testData.totalCount}
    })
  })

  it("getCollectionByUUID should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const uuid = "uuid1"
    const path = "api/collections/" + uuid
    const date = new Date("2020-06-25T14:58:46.313Z")
    const testData: ApiCollection = new ApiCollection(createMockCollection(date, date, date, date, PublishStatus.Draft))

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.getCollectionByUUID(uuid)

    // Assert
    result$
      .subscribe((data: ApiCollection) => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("GET")
    req.flush(testData)
  })

  it("getCollectionJson should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const uuid = "uuid1"
    const path = "api/collections/" + uuid
    const date = new Date("2020-06-25T14:58:46.313Z")
    const testData: ApiCollection = new ApiCollection(createMockCollection(date, date, date, date, PublishStatus.Draft))

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.getCollectionJson(uuid)

    // Assert
    result$
      .subscribe((data: string) => {
        expect(data).toEqual(JSON.stringify(testData))
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("GET")
    req.flush(testData)
  })
  it("getCollectionJson should not return", () => {
    // Arrange
    const uuid: string = undefined as unknown as string
    const path = "api/collections/" + uuid

    // Act
    try {
      testService.getCollectionJson(uuid)
    } catch (e) {
      expect(e instanceof Error).toBeTrue()
      expect(e.message).toEqual("No uuid provided for collection json export")
    }

    // Assert
    httpTestingController.expectNone(AppConfig.settings.baseApiUrl + "/" + path)
  })

  it("getCollectionSkillsCsv should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const uuid = "f6aacc9e-bfc6-4cc9-924d-c7ef83afef07"
    const path = "api/collections/" + uuid + "/csv"
    const testData = createMockTaskResult()
    const expected = new ApiTaskResult(testData)

    // Act
    const result$ = testService.requestCollectionSkillsCsv(uuid)

    // Assert
    result$
      .subscribe((data: ITaskResult) => {
        expect(data).toEqual(expected)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("GET")
    req.flush(testData)
  })
  it("getCollectionSkillsCsv should not return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const uuid: string = undefined as unknown as string
    const path = "api/collections/" + uuid + "/csv"

    // Act
    try {
      testService.requestCollectionSkillsCsv(uuid)
    } catch (e) {
      expect(e instanceof Error).toBeTrue()
      expect(e.message).toEqual("Invalid collection uuid.")
    }

    // Assert
    httpTestingController.expectNone(AppConfig.settings.baseApiUrl + "/" + path)
  })

  it("getCsvTaskResultsIfComplete should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const uuid = "f6aacc9e-bfc6-4cc9-924d-c7ef83afef07"
    const path = "api/results/text/" + uuid
    const testData = { foo: "bar" }
    const expected = testData

    // Act
    const result$ = testService.getCsvTaskResultsIfComplete(uuid)

    // Assert
    result$
      .subscribe((data: HttpResponse<{ foo: string}>) => {
        expect(data.body?.toString()).toEqual(JSON.stringify(expected))
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("GET")
    req.flush(testData)
  })

  it("getCollectionSkills should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const uuid = "f6aacc9e-bfc6-4cc9-924d-c7ef83afef07"
    const path = "api/collections/" + uuid + "/skills?sort=undefined"
    const testData = createMockPaginatedSkills()
    const expected = testData

    // Act
    const result$ = testService.getCollectionSkills(uuid)

    // Assert
    result$
      .subscribe((data: PaginatedSkills) => {
        expect(data).toEqual(expected)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("POST")
    req.flush(testData.skills, {
      headers: { "x-total-count": "" + testData.totalCount}
    })
  })

  it("createCollection should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const path = "api/collections"
    const now = new Date()
    const testData = [
      new ApiCollection(createMockCollection(
        now, now, now, now,
        PublishStatus.Draft
        // The default is to have some skills
      ))
    ]
    const stringListUpdate: IStringListUpdate = { add: [], remove: [] }
    const expected = testData[0]
    expected.skills.forEach(s => stringListUpdate.add?.push(s))
    const input = new ApiCollectionUpdate({
      name: expected.name,
      description: expected.description,
      status: expected.status,
      author: expected.author,
      skills: stringListUpdate
    })

    // Act
    const result$ = testService.createCollection(input)

    // Assert
    result$
      .subscribe((data: ApiCollection) => {
        expect(data).toEqual(expected)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("POST")
    req.flush(testData)
  })

  it("updateCollection should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const now = new Date()
    const testData = new ApiCollection(createMockCollection(
        now, now, now, now,
        PublishStatus.Draft
        // The default is to have some skills
      ))
    const stringListUpdate: IStringListUpdate = { add: [], remove: [] }
    const expected = testData
    const uuid = expected.uuid
    const path = "api/collections/" + uuid + "/update"
    expected.skills.forEach(s => stringListUpdate.add?.push(s))
    const input = new ApiCollectionUpdate({
      name: expected.name,
      description: expected.description,
      status: expected.status,
      author: expected.author,
      skills: stringListUpdate
    })

    // Act
    const result$ = testService.updateCollection(uuid, input)

    // Assert
    result$
      .subscribe((data: ApiCollection) => {
        expect(data).toEqual(expected)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("POST")
    req.flush(testData)
  })

  it("searchCollections should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const testData: PaginatedCollections = createMockPaginatedCollections()
    const expected = testData
    const path = "api/search/collections"
    const query = "testQueryString"
    const apiSearch = new ApiSearch({ query })
    const size = 5
    const from = 1
    const filter = new Set<PublishStatus>([PublishStatus.Published, PublishStatus.Draft])
    const sort = ApiSortOrder.SkillAsc

    // Act
    const result$ = testService.searchCollections(apiSearch, size, from, filter, sort)

    // Assert
    result$
      .subscribe((data: PaginatedCollections) => {
        expect(data).toEqual(expected)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path +
        `?sort=${sort}&status=${PublishStatus.Published}&status=${PublishStatus.Draft}&size=${size}&from=${from}`)
    expect(req.request.method).toEqual("POST")
    req.flush(testData.collections, {
      headers: { "x-total-count": "" + testData.totalCount}
    })
  })

  it("updateSkills should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const testData = createMockTaskResult()
    const expected = new ApiTaskResult(testData)
    const uuid = expected.uuid
    const path = "api/collections/" + uuid + "/updateSkills"
    const query = "testQueryString"
    const update = new ApiSkillListUpdate({
      add: new ApiSearch({query}),
      remove: new ApiSearch({query})
    })
    const filter = new Set<PublishStatus>([PublishStatus.Published, PublishStatus.Draft])
    const sort = undefined

    // Act
    const result$ = testService.updateSkills(uuid, update, filter)

    // Assert
    result$
      .subscribe((data: ITaskResult) => {
        expect(data).toEqual(expected)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path +
      `?sort=${sort}&status=${PublishStatus.Published}&status=${PublishStatus.Draft}`)
    expect(req.request.method).toEqual("POST")
    req.flush(testData)
  })

  it("delete collection with result should works", fakeAsync(() => {
    const result$ = testService.deleteCollectionWithResult(apiTaskResultForDeleteCollection.uuid)
    tick(ASYNC_WAIT_PERIOD)
    // Assert
    result$.subscribe((data: ApiBatchResult) => {
      expect(RouterData.commands).toEqual([]) // No Errors
    })
    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + `/api/collections/${apiTaskResultForDeleteCollection.uuid}/remove`)
    expect(req.request.method).toEqual("DELETE")
    expect(req.request.headers.get("Accept")).toEqual("application/json")
  }))

  it("updateSkillsWithResult should return", fakeAsync(() => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const taskResult = createMockTaskResult()
    const apiBatchResult = new ApiBatchResult(createMockBatchResult())
    const expected = apiBatchResult
    const uuid: string = taskResult.uuid ? taskResult.uuid : ""
    const path1 = "api/collections/" + uuid + "/updateSkills"
    const path2 = taskResult.id
    const query = "testQueryString"
    const update = new ApiSkillListUpdate({
      add: new ApiSearch({query}),
      remove: new ApiSearch({query})
    })
    const filter = new Set<PublishStatus>([PublishStatus.Published, PublishStatus.Draft])
    const sort = undefined

    // Act
    const result$ = testService.updateSkillsWithResult(uuid, update, filter)

    // Assert
    result$
      .subscribe((data: ApiBatchResult) => {
        expect(data).toEqual(expected)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    /* Service call will make 2 requests: the requested action + the async task result */
    /* Setup for request 1 */
    const req1 = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path1 +
      `?sort=${sort}&status=${PublishStatus.Published}&status=${PublishStatus.Draft}`)
    expect(req1.request.method).toEqual("POST")
    req1.flush(taskResult)

    tick(ASYNC_WAIT_PERIOD)

    /* Setup for request 2 */
    const req2 = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path2)
    expect(req2.request.method).toEqual("GET")
    req2.flush(apiBatchResult)
  }))

  // An example of how to test a multiple requests.  In this case, one service request precipitated the next.
  it("publishCollectionWithResult should return", fakeAsync(() => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const taskResult = createMockTaskResult()
    const apiBatchResult = new ApiBatchResult(createMockBatchResult())
    const expected = apiBatchResult
    const path1 = "api/collections/publish"
    const path2 = taskResult.id
    const query = "testQueryString"
    const apiSearch = new ApiSearch({ query })
    const filter = new Set<PublishStatus>([PublishStatus.Draft])
    const newStatus = PublishStatus.Published

    // Act
    const result$ = testService.publishCollectionsWithResult(apiSearch, newStatus, filter)

    // Assert
    result$
      .subscribe((data: ApiBatchResult) => {
        expect(data).toEqual(expected)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    /* Service call will make 2 requests: the requested action + the async task result */
    /* Setup for request 1 */
    const req1 = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path1 +
      `?newStatus=${newStatus}&filterByStatus=${PublishStatus.Draft}`)
    expect(req1.request.method).toEqual("POST")
    req1.flush(taskResult)

    tick(ASYNC_WAIT_PERIOD)

    /* Setup for request 2 */
    const req2 = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path2)
    expect(req2.request.method).toEqual("GET")
    req2.flush(apiBatchResult)
  }))

  it("collectionReadyToPublish should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const uuid = "f6aacc9e-bfc6-4cc9-924d-c7ef83afef07"
    const path = "api/collections/" + uuid + "/skills"
    const testData = createMockPaginatedSkills(0, 0)
    const expected = true
    const size = 1
    const from = 0
    const sort = undefined

    // Act
    const result$ = testService.collectionReadyToPublish(uuid)

    // Assert
    result$
      .subscribe((data: boolean) => {
        expect(data).toEqual(expected)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path +
      `?sort=${sort}&status=${PublishStatus.Archived}&status=${PublishStatus.Draft}&size=${size}&from=${from}`)
    expect(req.request.method).toEqual("POST")
    req.flush(testData.skills, {
      headers: { "x-total-count": "" + testData.totalCount}
    })
  })
})
