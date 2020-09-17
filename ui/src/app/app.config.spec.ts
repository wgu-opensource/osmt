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
    it("should load a config from uri", () => {
      const dummyConfig: IAppConfig = {
        baseApiUrl: "https://unit-test.wgu.dev"
      }

      environment.whitelabelConfigUri = "www.url.com"

      service.load().finally(() => {
        expect(AppConfig.settings.baseApiUrl).toBe(dummyConfig.baseApiUrl)
      })

      const req = httpMock.expectOne(`www.url.com`)
      expect(req.request.method).toBe("GET")
      req.flush(dummyConfig)
    })
  })

})

