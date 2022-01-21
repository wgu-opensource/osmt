import {AppConfig} from "./app.config"
import {getTestBed, TestBed} from "@angular/core/testing"
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing"
import {IAppConfig} from "./models/app-config.model"
import {environment} from "../environments/environment"
import {APP_INITIALIZER} from "@angular/core"
import {initializeApp} from "./app.module"

describe("AppConfig", () => {
  let injector: TestBed
  let service: AppConfig
  let httpMock: HttpTestingController

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AppConfig,
        { provide: APP_INITIALIZER,
          useFactory: initializeApp,
          deps: [AppConfig], multi: true }
      ]
    })
    injector = getTestBed()
    service = TestBed.inject(AppConfig)
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
        loginUrl: "",
        isAuthEnabled: false,
        production: false,
        editableAuthor: false,
        defaultAuthorValue: "",
        toolName: "",
        toolNameLong: "",
        publicSkillTitle: "",
        publicCollectionTitle: "",
        licensePrimary: "",
        licenseSecondary: "",
        poweredBy: "",
        poweredByUrl: "",
        poweredByLabel: "",
        idleTimeoutInSeconds: 300
      }

      environment.baseApiUrl = expectedApiUrl
      environment.dynamicWhitelabel = false

      service.load().finally(() => {
        expect(AppConfig.settings.baseApiUrl).toBe(dummyConfig.baseApiUrl)
      })

    })
  })

})

