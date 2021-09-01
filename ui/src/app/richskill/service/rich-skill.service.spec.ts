import { Location } from "@angular/common"
import { HttpClient } from "@angular/common/http"
import { HttpClientTestingModule, HttpTestingController } from "@angular/common/http/testing"
import { TestBed } from "@angular/core/testing"
import { Router } from "@angular/router"
import { createMockPaginatedSkills, createMockSkill } from "../../../../test/resource/mock-data"
import { AuthServiceData, AuthServiceStub, RouterData, RouterStub } from "../../../../test/resource/mock-stubs"
import { AppConfig } from "../../app.config"
import { AuthService } from "../../auth/auth-service"
import { EnvironmentService } from "../../core/environment.service"
import { PublishStatus } from "../../PublishStatus"
import { ApiSkill, ApiSortOrder } from "../ApiSkill"
import { PaginatedSkills } from "./rich-skill-search.service"
import { RichSkillService } from "./rich-skill.service"


// An example of how to test a service


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

  it("getSkillsByUUID should return", () => {
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
})
