import {fakeAsync, TestBed, tick} from "@angular/core/testing"
import { HttpClientTestingModule, HttpTestingController } from "@angular/common/http/testing"
import { NamedReferenceService } from "./named-reference.service"
import { Location } from "@angular/common"
import { Router } from "@angular/router"
import { EnvironmentService } from "../../../core/environment.service"
import { AppConfig } from "../../../app.config"
import { AuthService } from "../../../auth/auth-service"
import {AuthServiceData, AuthServiceStub, RouterData, RouterStub} from "@test/resource/mock-stubs"
import { PaginatedMetadata } from "../../PaginatedMetadata";
import {
  createMockNamedReference2,
  createMockPaginatedMetaDataWithNamedReferences
} from "@test/resource/mock-data";
import { ApiSortOrder } from "../../../richskill/ApiSkill";
import { MetadataType } from "../../rsd-metadata.enum";
import { ApiNamedReference, ApiNamedReferenceUpdate } from "../NamedReference";
import { ApiBatchResult } from "../../../richskill/ApiBatchResult";
import { getBaseApi } from "../../../api-versions"

const ASYNC_WAIT_PERIOD = 3000

describe("NamedReferenceService", () => {
  let testService: NamedReferenceService

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        EnvironmentService,
        AppConfig,
        NamedReferenceService,
        Location,
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: Router, useClass: RouterStub },
        {
          provide: "BASE_API",
          useFactory: getBaseApi,
        }
      ]})
    testService = TestBed.inject(NamedReferenceService)
    let httpTestingController = TestBed.inject(HttpTestingController)
    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()
  })

  it("should be created", () => {
    expect(testService).toBeTruthy()
  })

  it("getNamedReferences() should return Array of NamedReferences", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const path = getBaseApi() + "/metadata/keywords?size=3&from=0&sort=name.asc&query=&type=categories"
    const testData: PaginatedMetadata = createMockPaginatedMetaDataWithNamedReferences(3, 10)
    let httpTestingController = TestBed.inject(HttpTestingController)

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.paginatedNamedReferences(testData.data.length, 0, ApiSortOrder.KeywordNameAsc,MetadataType.Category,  undefined)

    // Assert
    result$
      .subscribe((data: PaginatedMetadata) => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + path)
    expect(req.request.method).toEqual("GET")
    req.flush(testData.data, {
      headers: { "x-total-count": "" + testData.totalCount}
    })
  })

  it("getNamedReferenceById() should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const id = "12345"
    const path = getBaseApi() + "/metadata/keywords/" + id
    const testData: ApiNamedReference = new ApiNamedReference(createMockNamedReference2())
    let httpTestingController = TestBed.inject(HttpTestingController)

    // Act
    // noinspection LocalVariableNamingConventionJS
    const result$ = testService.getNamedReferenceById(id)

    // Assert
    result$
      .subscribe((data: ApiNamedReference) => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + path)
    expect(req.request.method).toEqual("GET")
    req.flush(testData)
  })

  it("createNamedReference() should return a Named Reference", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const path = getBaseApi() + "/metadata/keywords"
    const testData = [
      new ApiNamedReference(createMockNamedReference2())
    ]
    const expected = testData[0]
    const input = new ApiNamedReferenceUpdate({
      name : expected.name,
      type : expected.type,
      url : expected.url,
      framework : expected.framework
    })
    let httpTestingController = TestBed.inject(HttpTestingController)

    // Act
    const result$ = testService.createNamedReference(input)

    // Assert
    result$
      .subscribe((data: ApiNamedReference) => {
        expect(data).toEqual(expected)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + path)
    expect(req.request.method).toEqual("POST")
    req.flush(testData)
  })

  it("updateNamedReference() should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const testData = new ApiNamedReference(createMockNamedReference2())
    const expected = testData
    const id = expected.id
    const path = getBaseApi() + "/metadata/keywords/" + id
    const input = new ApiNamedReferenceUpdate({
      framework : expected.framework,
      name : expected.name,
      type : expected.type,
      url : expected.url
    })
    let httpTestingController = TestBed.inject(HttpTestingController)

    // Act
    const result$ = testService.updateNamedReference(id, input)

    // Assert
    result$
      .subscribe((data: ApiNamedReference) => {
        expect(data).toEqual(expected)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + path + "/update")
    expect(req.request.method).toEqual("POST")
    req.flush(testData)
  })

  it("deleteNamedReferenceWithResult() should work", fakeAsync(() => {
    const namedReferenceId = 2
    const result$ = testService.deleteNamedReferenceWithResult(namedReferenceId)
    tick(ASYNC_WAIT_PERIOD)
    let httpTestingController = TestBed.inject(HttpTestingController)

    // Assert
    result$.subscribe((data: ApiBatchResult) => {
      expect(RouterData.commands).toEqual([]) // No Errors
    })
    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + `/api/metadata/keywords/${namedReferenceId}/remove`)
    expect(req.request.method).toEqual("DELETE")
    expect(req.request.headers.get("Accept")).toEqual("application/json")
  }))
})
