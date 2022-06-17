import {AppConfig} from "./app.config"
import {getTestBed, TestBed} from "@angular/core/testing"
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing"
import {EnvironmentService} from "./core/environment.service"
import {IAppConfig} from "./models/app-config.model"

describe("AppConfig", () => {
  let injector: TestBed
  let environmentService: EnvironmentService
  let settingsService: AppConfig
  let httpMock: HttpTestingController

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AppConfig,
        EnvironmentService,
      ]
    })

    const appConfig = TestBed.inject(AppConfig)
    AppConfig.settings = appConfig.defaultConfig()

    injector = getTestBed()
    environmentService = TestBed.inject(EnvironmentService)
    settingsService = TestBed.inject(AppConfig)
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
        clientId: "",
        clientIdHash: "",
        authUrl: "https://id.sbx.wgu.edu",
        logoutUrl: "https://id.sbx.wgu.edu/idp/startSLO.ping?redirect_url=\"https://localhost:4200\"",
        redirectUrl: "http://localhost:4200/login/success",
        idleTimeoutInSeconds: 300,
        dynamicWhitelabel: true
      }

      environmentService.environment.baseApiUrl = expectedApiUrl
      environmentService.environment.dynamicWhitelabel = false

      settingsService.load().finally(() => {
        expect(environmentService.environment.baseApiUrl).toBe(dummyConfig.baseApiUrl)
        expect(AppConfig.settings.baseApiUrl).toBe(dummyConfig.baseApiUrl)
      })

    })
  })

})

