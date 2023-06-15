import { Location } from "@angular/common"
import { HttpClient, HttpClientModule } from "@angular/common/http"
import { HttpClientTestingModule, HttpTestingController } from "@angular/common/http/testing"
import { Inject, Injectable } from "@angular/core"
import { async, TestBed } from "@angular/core/testing"
import { Data, Router } from "@angular/router"
import { Observable, of } from "rxjs"
import { map } from "rxjs/operators"
import { createMockTaskResult } from "@test/resource/mock-data"
import { AuthServiceData, AuthServiceStub, RouterData, RouterStub } from "@test/resource/mock-stubs"
import { AbstractService } from "./abstract.service"
import { AppConfig } from "./app.config"
import { AuthService } from "./auth/auth-service"
import { EnvironmentService } from "./core/environment.service"
import { PublishStatus } from "./PublishStatus"
import { ApiSortOrder } from "./richskill/ApiSkill"
import { ApiSearch } from "./richskill/service/rich-skill-search.service"
import { ApiTaskResult } from "./task/ApiTaskResult"
import { getBaseApi } from "./api-versions"


@Injectable({
  providedIn: "root"
})
export class ConcreteService extends AbstractService {
  constructor(
    httpClient: HttpClient,
    authService: AuthService,
    router: Router,
    location: Location,
    @Inject("BASE_API") baseApi: string
  ) {
    super(httpClient, authService, router, location, baseApi)
  }

  public buildUrl(path: string): string {
    return super.buildUrl(path)
  }

  public safeUnwrapBody<T>(body: T | null, failureMessage: string): T {
    return super.safeUnwrapBody(body, failureMessage)
  }
}

interface IWork {
  foo: string  // Data def that is not likely to collide with real code
}
class Work implements IWork {  // This just follows the pattern used throughout the source code
  foo: string
  constructor(work: IWork) {
    this.foo = work.foo
  }

  doWork(id: string): Observable<ApiTaskResult> {
    return of(new ApiTaskResult({
      status: PublishStatus.Draft,
      contentType: "my content type",
      id,
      uuid: "my collection summary uuid"
    }))
  }
}


describe("AbstractService (no HTTP needed)", () => {
  let router: RouterStub
  let authService: AuthServiceStub
  let testService: ConcreteService

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [],
      imports: [
        HttpClientModule
      ],
      providers: [
        ConcreteService,
        Location,
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: Router, useClass: RouterStub },
        {
          provide: "BASE_API",
          useFactory: getBaseApi,
        },
      ]
    })

    router = TestBed.inject(Router)
    authService = TestBed.inject(AuthService)
    testService = TestBed.inject(ConcreteService)
  }))

  it("should be created", () => {
    expect(testService).toBeTruthy()
  })

  it("redirectToLogin with no status should ignore it", () => {
    [
      { input: undefined,
        output: { commands: [], isDown: false }},
      { input: { },
        output: { commands: [], isDown: false }},
      // Ignoring 401 case because the behavior is configurable.
      { input: { status: 0 },
        output: { commands: [], isDown: true }}
    ].forEach((params) => {
      // Arrange
      RouterData.commands = []
      AuthServiceData.isDown = false

      // Act
      testService.redirectToLogin(params.input)

      // Assert
      expect(RouterData.commands).toEqual(params.output.commands)
      expect(AuthServiceData.isDown).toEqual(params.output.isDown)
    })
  })

  it("buildTableParams should be correct", () => {
    // Arrange
    const size = 5
    const from = 1
    const filter = new Set<PublishStatus>([PublishStatus.Published, PublishStatus.Draft])
    const sort = ApiSortOrder.NameAsc

    // Act
    const params = testService.buildTableParams(
      size,
      from,
      filter,
      sort
    )

    // Assert
    expect(params).toEqual({
      size,
      from,
      status: Array.from(filter).map(s => s.toString()),
      sort: sort.toString()
    })
  })
})

describe("AbstractService (HTTP needed)", () => {
  let httpClient: HttpClient
  let httpTestingController: HttpTestingController
  let router: RouterStub
  let authService: AuthServiceStub
  let testService: ConcreteService

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [],
      imports: [
        HttpClientTestingModule
      ],
      providers: [
        EnvironmentService,
        AppConfig,
        ConcreteService,
        Location,
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: Router, useClass: RouterStub },
        {
          provide: "BASE_API",
          useFactory: getBaseApi,
        }
      ]
    })
    .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()

    httpClient = TestBed.inject(HttpClient)
    httpTestingController = TestBed.inject(HttpTestingController)
    router = TestBed.inject(Router)
    authService = TestBed.inject(AuthService)
    testService = TestBed.inject(ConcreteService)
  }))

  afterEach(() => {
    httpTestingController.verify()
  })

  it("should be created", () => {
    expect(testService).toBeTruthy()
  })

  it("buildUrl should return", () => {
    const path = "data"
    expect(testService.buildUrl(path)).toEqual(AppConfig.settings.baseApiUrl + getBaseApi() + "/" + path)
  })

  it("buildUrl should works with tasks", () => {
    const mockTaskResult = createMockTaskResult()
    const builtUrl = testService.buildUrl(mockTaskResult.id.slice(1))
    const builtUrlWithSlice = testService.buildUrl(mockTaskResult.id)
    expect(builtUrl).toEqual(AppConfig.settings.baseApiUrl + mockTaskResult.id)
    expect(builtUrlWithSlice).toEqual(AppConfig.settings.baseApiUrl + mockTaskResult.id)
  })

  /* See https://angular.io/guide/http#testing-http-requests */
  it("get should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const path = "any/path"
    const testData: Data = { foo: "bar" }  // Data that is unlikely to exist in any AbstractService derivative

    // Act
    const result$ = testService.get<Data>({ path })

    // Assert
    result$
      .pipe(map(({body}) => body))  // Convert from HttpResponse<Data> to just Data
      .subscribe(data => {
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + getBaseApi() + "/" + path)
    expect(req.request.method).toEqual("GET")
    req.flush(testData)
  })

  /* See https://angular.io/guide/http#testing-http-requests */
  it("post should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const path = "any/path"
    const fullUrl = `${AppConfig.settings.baseApiUrl}${getBaseApi()}/${path}`
    const testData: Data = { foo: "bar" }  // Data that is unlikely to exist in any AbstractService derivative
    const errorMsg = `Could not unwrap Data exception...`

    // Act
    const result$ = testService.post<Data>({ path })

    // Assert
    result$
      .pipe(map(({body}) => body))  // Convert from HttpResponse<Data> to just Data
      .subscribe(unsafe => {
        const data = testService.safeUnwrapBody<Data>(unsafe, errorMsg)
        expect(data).toEqual(testData)
        expect(RouterData.commands).toEqual([ ])  // No errors
        expect(AuthServiceData.isDown).toEqual(false)
      })

    const req = httpTestingController.expectOne(fullUrl)
    expect(req.request.method).toEqual("POST")
    req.flush(testData)
  })

  it("bulkStatusChange should return", () => {
    // Arrange
    const newStatus = PublishStatus.Draft
    const path = "/any/path"
    const fullUrl = `${AppConfig.settings.baseApiUrl}${getBaseApi()}/${path}?newStatus=${newStatus.toString()}`
    const query = "my query string"
    const apiSearch = new ApiSearch({ query })
    const expected = new ApiTaskResult(createMockTaskResult())

    // Act
    const result$ = testService.bulkStatusChange(
      path,
      apiSearch,
      newStatus
    )

    // Assert
    result$
      .subscribe(data =>
        expect(data).toEqual(expected)
      )

    const req = httpTestingController.expectOne(fullUrl)
    expect(req.request.method).toEqual("POST")
    req.flush(expected)
  })

  it("pollForTaskResult should return", (done) => {
    // Arrange
    const testData: IWork = { foo: "bar" }  // Data that is unlikely to exist in any AbstractService derivative
    const path = "tasks/42"
    const fullUrl = AppConfig.settings.baseApiUrl + getBaseApi() + "/" + path
    const worker = new Work(testData)

    // Act
    testService.pollForTaskResult<IWork>(worker.doWork(path), 1000)
      .subscribe((data) => {
        expect(data).toEqual(testData)
        done()
      })
    const req = httpTestingController.expectOne(fullUrl)
    req.flush(testData)

    // Assert
    expect(req.request.method).toEqual("GET")
  })

  it("pollForTaskResult should error", (done) => {
    // Arrange
    const testData: IWork = { foo: "bar" }  // Data that is unlikely to exist in any AbstractService derivative
    const path = "/tasks/42"
    const fullUrl = AppConfig.settings.baseApiUrl + getBaseApi() + "/" + path
    const worker = new Work(testData)

    // Act
    testService.pollForTaskResult<IWork>(worker.doWork(path), 1000)
      .subscribe(
        (data) => {
          expect(data).toBeFalsy()
          done()
        })
    const req = httpTestingController.expectOne(fullUrl)
    req.flush("Missing", { status: 404, statusText: "Not found" })

    // Assert
    expect(req.request.method).toEqual("GET")
  })
})
