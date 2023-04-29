import { TestBed } from "@angular/core/testing"
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing"
import {JobCodeService, PaginatedJobCodes} from "./job-code.service"
import {AuthServiceData, AuthServiceStub, RouterData, RouterStub} from "../../../../test/resource/mock-stubs"
import {AppConfig} from "../../app.config"
import {EnvironmentService} from "../../core/environment.service"
import {Location} from "@angular/common"
import {AuthService} from "../../auth/auth-service"
import {Router} from "@angular/router"
import {createMockJobcode, createMockPaginatedJobCodes} from "../../../../test/resource/mock-data"
import {ApiSortOrder} from "../../richskill/ApiSkill"
import {ApiJobCode, ApiJobCodeUpdate, } from "../Jobcode"


describe("JobCodeService", () => {
  let testService: JobCodeService
  let httpTestingController: HttpTestingController


  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        EnvironmentService,
        AppConfig,
        JobCodeService,
        Location,
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: Router, useClass: RouterStub }
      ]}).compileComponents()
    testService = TestBed.inject(JobCodeService)
    httpTestingController = TestBed.inject(HttpTestingController)
    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()
  })

  it("JobCode should be created", () => {
    expect(testService).toBeTruthy()
  })

  it("getJobCodes should return Array of JobCodes", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const path = "api/job-codes?sort=name.asc&size=3&from=0"
    const testData: PaginatedJobCodes = createMockPaginatedJobCodes(3, 10)

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.getJobCodes(testData.jobCodes.length, 0, ApiSortOrder.NameAsc)

    // Assert
    result$
      .subscribe((data: PaginatedJobCodes) => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("GET")
    req.flush(testData.jobCodes, {
      headers: { "x-total-count": "" + testData.totalCount}
    })
  })

  it("createJobCode should return a JobCode", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const path = "api/job-codes"
    const now = new Date()
    const testData = [
      new ApiJobCode(createMockJobcode())
    ]
    const expected = testData[0]
    const input = new ApiJobCodeUpdate({
      code : expected.code,
      targetNodeName : expected.targetNodeName,
      targetNode : expected.targetNode,
      frameworkName : expected.frameworkName,
      level : expected.level,
      parents : []
    })

    // Act
    const result$ = testService.createJobCode(input)

    // Assert
    result$
      .subscribe((data: ApiJobCode) => {
        expect(data).toEqual(expected)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("POST")
    req.flush(testData)
  })
})
