import {Location} from "@angular/common"
import {HttpClient} from "@angular/common/http"
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing"
import {async, TestBed} from "@angular/core/testing"
import {Router} from "@angular/router"
import {
  AuthServiceData,
  AuthServiceStub, CategoryServiceData,
  CategoryServiceStub,
  RouterData,
  RouterStub
} from "../../../../test/resource/mock-stubs"
import {AppConfig} from "../../app.config"
import {AuthService} from "../../auth/auth-service"
import {EnvironmentService} from "../../core/environment.service"
import {CategoryService} from "./category.service"
import {ApiCategory, IKeyword, KeywordSortOrder, PaginatedCategories} from "../ApiCategory"
import { getBaseApi } from "../../api-versions"

describe("CategoryService", () => {
  let httpClient: HttpClient
  let httpTestingController: HttpTestingController
  let router: RouterStub
  let authService: AuthServiceStub
  let testService: CategoryService

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [],
      imports: [
        HttpClientTestingModule,
      ],
      providers: [
        EnvironmentService,
        AppConfig,
        CategoryService,
        Location,
        { provide: AuthService, useClass: AuthServiceStub },
        { provide: Router, useClass: RouterStub },
        {
          provide: "BASE_API",
          useFactory: getBaseApi,
        },
      ]
    })
      .compileComponents()

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()  // This avoids the race condition on reading the config's whitelabel.toolName

    httpClient = TestBed.inject(HttpClient)
    httpTestingController = TestBed.inject(HttpTestingController)
    router = TestBed.inject(Router)
    authService = TestBed.inject(AuthService)
    testService = TestBed.inject(CategoryService)
  }))

  afterEach(() => {
    httpTestingController.verify()
  })

  it("should be created", () => {
    expect(testService).toBeTruthy()
  })

  it("getAllPaginated should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const sort = KeywordSortOrder.SkillCountDesc
    const size = 10
    const from = 20
    const path = `${getBaseApi()}/categories?sort=${sort}&size=${size}&from=${from}`

    // Act
    const result = testService.getAllPaginated(size, from, sort)

    // Assert
    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl + path)
    expect(req.request.method).toEqual("GET")
    req.flush(CategoryServiceData.paginatedCategories, {
      headers: { "x-total-count": "" + CategoryServiceData.paginatedCategories.totalCount}
    })
  })

  it("getById should return", () => {
    // Arrange
    RouterData.commands = []
    AuthServiceData.isDown = false
    const path = `${getBaseApi()}/categories/${CategoryServiceData.category.id}`

    // Act
    const result$ = testService.getById(CategoryServiceData.category.id.toString())

    // Assert
    result$.subscribe((data: ApiCategory) => {
      expect(data).toEqual(CategoryServiceData.category)
      expect(RouterData.commands).toEqual([])
      expect(AuthServiceData.isDown).toEqual(false)
    })

    const req = httpTestingController.expectOne(AppConfig.settings.baseApiUrl  + path)
    expect(req.request.method).toEqual("GET")
    req.flush(CategoryServiceData.categoryKeyword)
  })
})
