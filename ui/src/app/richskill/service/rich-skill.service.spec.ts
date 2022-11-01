// noinspection LocalVariableNamingConventionJS

import { Location } from "@angular/common"
import { HttpClient } from "@angular/common/http"
import { HttpClientTestingModule, HttpTestingController } from "@angular/common/http/testing"
import { fakeAsync, TestBed, tick } from "@angular/core/testing"
import { Router } from "@angular/router"
import {
  createMockAuditLog,
  createMockBatchResult,
  createMockPaginatedSkills,
  createMockSkill,
  createMockSkillSummary,
  createMockSkillUpdate,
  createMockTaskResult
} from "../../../../test/resource/mock-data"
import { AuthServiceData, AuthServiceStub, RouterData, RouterStub } from "../../../../test/resource/mock-stubs"
import { AppConfig } from "../../app.config"
import { AuthService } from "../../auth/auth-service"
import { EnvironmentService } from "../../core/environment.service"
import { PublishStatus } from "../../PublishStatus"
import { ApiBatchResult } from "../ApiBatchResult"
import { ApiAuditLog, ApiSkill, ApiSortOrder } from "../ApiSkill"
import { ApiSkillSummary } from "../ApiSkillSummary"
import { ApiSkillUpdate } from "../ApiSkillUpdate"
import { ApiSearch, PaginatedSkills } from "./rich-skill-search.service"
import { RichSkillService } from "./rich-skill.service"


// An example of how to test a service


const ASYNC_WAIT_PERIOD = 3000

describe("RichSkillService", () => {
  let httpClient: HttpClient
  let httpTestingController: HttpTestingController
  let router: RouterStub
  let authService: AuthServiceStub
  let testService: RichSkillService

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [],
      imports: [
        HttpClientTestingModule
      ],
      providers: [
        EnvironmentService,
        AppConfig,
        RichSkillService,
        Location,
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: Router, useClass: RouterStub }
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()

    httpClient = TestBed.inject(HttpClient)
    httpTestingController = TestBed.inject(HttpTestingController)
    router = TestBed.inject(Router)
    authService = TestBed.inject(AuthService)
    testService = TestBed.inject(RichSkillService)
  })

  afterEach(() => {
    httpTestingController.verify()
  })

  it("should be created", () => {
    expect(testService).toBeTruthy()
  })

  it("getSkills should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const path = "api/skills?sort=name.asc&status=Draft&size=3&from=0"
    const testData: PaginatedSkills = createMockPaginatedSkills(3, 10)
    const statuses = new Set<PublishStatus>([ PublishStatus.Draft ])

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.getSkills(testData.skills.length, 0, statuses, ApiSortOrder.NameAsc)

    // Assert
    result$
      .subscribe((data: PaginatedSkills) => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("GET")
    req.flush(testData.skills, {
      headers: { "x-total-count": "" + testData.totalCount}
    })
  })

  it("getSkillByUUID should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const uuid = "uuid1"
    const path = "api/skills/" + uuid
    const date = new Date("2020-06-25T14:58:46.313Z")
    const testData: ApiSkill = new ApiSkill(createMockSkill(date, date, PublishStatus.Draft))

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.getSkillByUUID(uuid)

    // Assert
    result$
      .subscribe((data: ApiSkill) => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("GET")
    req.flush(testData)
  })

  it("getSkillCsvByUUID should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const uuid = "f6aacc9e-bfc6-4cc9-924d-c7ef83afef07"
    const path = "api/skills/" + uuid
    const testData =
      "\"Canonical URL\",\"RSD Name\",\"Author\",\"Skill Statement\",\"Category\",\"Keywords\",\"Standards\",\"Certifications\",\"Occupation Major Groups\",\"Occupation Minor Groups\",\"Broad Occupations\",\"Detailed Occupations\",\"O*Net Job Codes\",\"Employers\",\"Alignment Name\",\"Alignment URL\"\n" +
      "\"https://localhost:8080/api/skills/f6aacc9e-bfc6-4cc9-924d-c7ef83afef07\",\"Situational Parameters\",\"Western Governors University\",\"Identify appropriate modes of written communication based on situational parameters.\",\"Written Communication\",\"SEL: Interpersonal Communication; Written Communication; Writing; Academic Writing\",\"\",\"\",\"29-0000\",\"29-1000\",\"29-1140\",\"29-1141\",\"29-1141.00; 29-1141.01; 29-1141.02; 29-1141.03; 29-1141.04\",\"Health Open Skills\",\"Written Communication\",\"skills.emsidata.com/skills/KS4425C63RPH46FJ9BX7\""

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.getSkillCsvByUuid(uuid)

    // Assert
    result$
      .subscribe((data: string) => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("GET")
    expect(req.request.headers.get("Accept")).toEqual("text/csv")
    req.flush(testData)
  })
  it("getSkillCsvByUUID should fail", () => {
    // Arrange
    const uuid = "f6aacc9e-bfc6-4cc9-924d-c7ef83afef07"

    try {
      // Act
      testService.getSkillCsvByUuid("")
    } catch (e) {
      // Assert
      expect(e.message).toEqual("No uuid provided for single skill csv export")
    }
  })

  it("getSkillJsonByUUID should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const uuid = "f6aacc9e-bfc6-4cc9-924d-c7ef83afef07"
    const path = "api/skills/" + uuid
    const testData =  // Doesn't matter what the string has in it.
      `{
          "foo": "bar"
       }`

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.getSkillJsonByUuid(uuid)

    // Assert
    result$
      .subscribe((data: string) => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("GET")
    expect(req.request.headers.get("Accept")).toEqual("application/json")
    req.flush(testData)
  })
  it("getSkillJsonByUuid should fail", () => {
    // Arrange
    const uuid = "f6aacc9e-bfc6-4cc9-924d-c7ef83afef07"

    try {
      // Act
      testService.getSkillJsonByUuid("")
    } catch (e) {
      // Assert
      expect(e.message).toEqual("No uuid provided for single skill json export")
    }
  })

  it("createSkill should return", fakeAsync(() => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const taskResult = createMockTaskResult()
    const now = new Date()
    const skillResult: ApiSkill = new ApiSkill(createMockSkill(now, now, PublishStatus.Published))
    const expected = skillResult
    const path1 = "api/skills"
    const path2 = taskResult.id
    const skillUpdate = createMockSkillUpdate()

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.createSkill(skillUpdate)

    // Assert
    result$
      .subscribe((data: ApiSkill) => {
        expect(data).toEqual(expected)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    /* Service call will make 2 requests: the requested action + the async task result */
    /* Setup for request 1 */
    const req1 = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path1)
    expect(req1.request.method).toEqual("POST")
    req1.flush(taskResult)

    tick(ASYNC_WAIT_PERIOD)

    /* Setup for request 2 */
    const req2 = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path2)
    expect(req2.request.method).toEqual("GET")
    req2.flush([skillResult])
  }))

  it("updateSkill should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const uuid = "f6aacc9e-bfc6-4cc9-924d-c7ef83afef07"
    const path = "api/skills/" + uuid + "/update"
    const skillUpdate: ApiSkillUpdate = createMockSkillUpdate()
    const now = new Date()
    const testData: ApiSkill = new ApiSkill(createMockSkill(now, now, PublishStatus.Draft))

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.updateSkill(uuid, skillUpdate)

    // Assert
    result$
      .subscribe((data: ApiSkill) => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("POST")
    req.flush(testData)
  })

  it("searchSkills should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const path = "api/search/skills"
    const query = "testQueryString"
    const apiSearch = new ApiSearch({ query })
    const size = 5
    const from = 1
    const filter = new Set<PublishStatus>([PublishStatus.Published, PublishStatus.Draft])
    const sort = ApiSortOrder.SkillAsc
    const testData = createMockPaginatedSkills()

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.searchSkills(apiSearch, size, from, filter, sort)

    // Assert
    result$
      .subscribe((data: PaginatedSkills) => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path +
      "?sort=skill.asc&status=Published&status=Draft&size=5&from=1")
    expect(req.request.method).toEqual("POST")
    req.flush(testData.skills, {
      headers: { "x-total-count": "" + testData.totalCount}
    })
  })

  it("libraryExport should return", () => {
    RouterData.commands = []

    // Act
    const result$ = testService.libraryExport()

    // Assert
    result$.subscribe( (data: string) => {
      expect(RouterData.commands).toEqual([]) // No Errors
    })

    const req = httpTestingController.expectOne("/api/export/library")
    expect(req.request.method).toEqual("GET")
    expect(req.request.headers.get("Accept")).toEqual("text/csv")
    req.flush(result$)
  })

  it("publishSkillsWithResult should return", fakeAsync(() => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const taskResult = createMockTaskResult()
    const apiBatchResult = new ApiBatchResult(createMockBatchResult())
    const expected = apiBatchResult
    const path1 = "api/skills/publish"
    const path2 = taskResult.id
    const query = "testQueryString"
    const apiSearch = new ApiSearch({ query })

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.publishSkillsWithResult(apiSearch)

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
            "?newStatus=Published")
    expect(req1.request.method).toEqual("POST")
    req1.flush(taskResult)

    tick(ASYNC_WAIT_PERIOD)

    /* Setup for request 2 */
    const req2 = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path2)
    expect(req2.request.method).toEqual("GET")
    req2.flush(apiBatchResult)
  }))

  it("auditLog should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const uuid = "f6aacc9e-bfc6-4cc9-924d-c7ef83afef07"
    const path = "api/skills/" + uuid + "/log"
    const auditLogs = [createMockAuditLog()]
    const testData: ApiAuditLog[] = [new ApiAuditLog(auditLogs[0])]

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.auditLog(uuid)

    // Assert
    result$
      .subscribe((data: ApiAuditLog[]) => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("GET")
    req.flush(auditLogs)
  })

  it("similarityCheck should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const statement = "my statement"
    const path = "api/search/skills/similarity"
    const skillSummary = [createMockSkillSummary()]
    const testData: ApiSkillSummary[] = [new ApiSkillSummary(skillSummary[0])]

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.similarityCheck(statement)

    // Assert
    result$
      .subscribe((data: ApiSkillSummary[]) => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("POST")
    req.flush(skillSummary)
  })

  it("similaritiesCheck should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const statements = ["my statement"]
    const path = "api/search/skills/similarities"
    const testData: boolean[] = [true]

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.similaritiesCheck(statements)

    // Assert
    result$
      .subscribe((data: boolean[]) => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("POST")
    req.flush(testData)
  })
})
