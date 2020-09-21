import {AppConfig} from "./app.config"
import {getTestBed, TestBed} from "@angular/core/testing"
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing"
import {IAppConfig} from "./models/app-config.model"
import {environment} from "../environments/environment"

describe("AppConfig", () => {
  let injector: TestBed
  let service: AppConfig
  let httpMock: HttpTestingController

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AppConfig]
    })
    injector = getTestBed()
    service = TestBed.inject(AppConfig)
    httpMock = TestBed.inject(HttpTestingController)
  })

  afterEach(() => {
    httpMock.verify()
  })

  describe("whitelabel configuration", () => {
    it("should load app config from environment", () => {
      const expectedApiUrl = "https://unit-test.osmt.dev"
      const dummyConfig: IAppConfig = {
        baseApiUrl: expectedApiUrl
      }

      environment.baseApiUrl = expectedApiUrl

      service.load().finally(() => {
        expect(AppConfig.settings.baseApiUrl).toBe(dummyConfig.baseApiUrl)
      })

    })
  })

})

