import { fakeAsync, TestBed, tick } from "@angular/core/testing"
import { HttpClientTestingModule, HttpTestingController } from "@angular/common/http/testing"
import { Location } from "@angular/common"
import { Router } from "@angular/router"
import { JobCodeService } from "./job-code.service"
import { AuthServiceData, AuthServiceStub, RouterData, RouterStub } from "@test/resource/mock-stubs"
import { AppConfig } from "../../../app.config"
import { EnvironmentService } from "../../../core/environment.service"
import { AuthService } from "../../../auth/auth-service"
import {
  apiTaskResultForDeleteJobCode,
  createMockJobcode,
  createMockPaginatedMetaDataWithJobCodes
} from "@test/resource/mock-data"
import { ApiSortOrder } from "../../../richskill/ApiSkill"
import { ApiJobCode, ApiJobCodeUpdate } from "../Jobcode"
import { ApiBatchResult } from "../../../richskill/ApiBatchResult"
import { PaginatedMetadata } from "../../PaginatedMetadata"
import { getBaseApi } from "../../../api-versions"

const ASYNC_WAIT_PERIOD = 3000

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
        { provide: Router, useClass: RouterStub },
        {
          provide: "BASE_API",
          useFactory: getBaseApi,
        }
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
    const path = "api/metadata/jobcodes?size=3&from=0&sort=name.asc&query="
    const testData: PaginatedMetadata = createMockPaginatedMetaDataWithJobCodes(3, 10)

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.paginatedJobCodes(testData.data.length, 0, ApiSortOrder.NameAsc, undefined)

    // Assert
    result$
      .subscribe((data: PaginatedMetadata) => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("GET")
    req.flush(testData.data, {
      headers: { "x-total-count": "" + testData.totalCount}
    })
  })

  it("getJobCodeById should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const id = "12345"
    const path = "api/metadata/jobcodes/" + id
    const testData: ApiJobCode = new ApiJobCode(createMockJobcode(42, "my jobcode name", id))

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.getJobCodeById(id)

    // Assert
    result$
      .subscribe((data: ApiJobCode) => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path)
    expect(req.request.method).toEqual("GET")
    req.flush(testData)
  })

  it("createJobCode should return a JobCode", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const path = "api/metadata/jobcodes"
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

  it("updateJobCode should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const testData = new ApiJobCode(createMockJobcode())
    const expected = testData
    const id = expected.code
    const path = "api/metadata/jobcodes/" + id
    const input = new ApiJobCodeUpdate({
      code: expected.code,
      targetNodeName: expected.targetNodeName,
      targetNode: expected.targetNode,
      frameworkName: expected.frameworkName,
      level: expected.level,
      parents: expected.parents
    })

    // Act
    const result$ = testService.updateJobCode(id, input)

    // Assert
    result$
      .subscribe((data: ApiJobCode) => {
        expect(data).toEqual(expected)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + "/" + path + "/update")
    expect(req.request.method).toEqual("POST")
    req.flush(testData)
  })

  it("deleteJobCodeWithResult() should works", fakeAsync(() => {
    const jobCodeId = 2
    const result$ = testService.deleteJobCodeWithResult(jobCodeId)
    tick(ASYNC_WAIT_PERIOD)
    // Assert
    result$.subscribe((data: ApiBatchResult) => {
      expect(RouterData.commands).toEqual([]) // No Errors
    })
    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + `/api/metadata/jobcodes/${jobCodeId}/remove`)
    expect(req.request.method).toEqual("DELETE")
    expect(req.request.headers.get("Accept")).toEqual("application/json")
  }))
})
