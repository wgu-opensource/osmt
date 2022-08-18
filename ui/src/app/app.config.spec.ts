import {AppConfig} from "./app.config"
import {getTestBed, TestBed} from "@angular/core/testing"
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing"
import {environment} from "../environments/environment"
import {IAppConfig} from "./models/app-config.model"

describe("AppConfig", () => {
  let injector: TestBed
  let settingsService: AppConfig
  let httpMock: HttpTestingController

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AppConfig,
      ]
    })

    injector = getTestBed()
    settingsService = TestBed.inject(AppConfig)
    AppConfig.settings = settingsService.defaultConfig()
    httpMock = TestBed.inject(HttpTestingController)
  })

  afterEach(() => {
    // httpMock.verify()
  })

  describe("whitelabel configuration", () => {
    it("should load app config from environment", () => {
      const expectedApiUrl = "https://unit-test.osmt.dev"

      const dummyConfig: IAppConfig = {
        baseApiUrl: expectedApiUrl,
        defaultAuthorValue: "",
        editableAuthor: false,
        licensePrimary: "",
        licenseSecondary: "",
        poweredBy: "",
        poweredByLabel: "",
        poweredByUrl: "",
        publicCollectionTitle: "",
        publicSkillTitle: "",
        toolName: "",
        toolNameLong: "",
        loginUrl: "",
        idleTimeoutInSeconds: 300
      }

      environment.baseApiUrl = expectedApiUrl
      environment.dynamicWhitelabel = false

      settingsService.load().finally(() => {
        expect(AppConfig.settings.baseApiUrl).toBe(dummyConfig.baseApiUrl)
      })

    })
  })

})

