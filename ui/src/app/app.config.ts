import { Injectable } from "@angular/core"
import {EnvironmentService} from "./core/environment.service"
import {DefaultAppConfig, IAppConfig} from "./models/app-config.model"
import {HttpClient} from "@angular/common/http"

// TODO: Adopt either AppConfig or EnvironmentService, but not both.

@Injectable()
export class AppConfig {

  static settings: IAppConfig
  private environment: IAppConfig

  constructor(
    private environmentService: EnvironmentService,
    private http: HttpClient
  ) {
    this.environment = this.environmentService.environment
  }

  defaultConfig(): IAppConfig {
    const settings = new DefaultAppConfig()
    settings.baseApiUrl = this.environment.baseApiUrl
    settings.clientId = this.environment.clientId
    settings.clientIdHash = this.environment.clientIdHash
    settings.redirectUrl = this.environment.redirectUrl
    settings.authUrl = this.environment.authUrl
    settings.logoutUrl = this.environment.logoutUrl
    return settings
  }

  load(): Promise<object> {
    const baseUrl = this.environment.baseApiUrl

    if (this.environment.dynamicWhitelabel) {
      return this.http.get(`/whitelabel/whitelabel.json`)
        .toPromise()
        .then(value => {
          AppConfig.settings = this.defaultConfig()
          Object.assign(AppConfig.settings, value as IAppConfig)

          // baseApiUrl and authUrl are not runtime whitelabellable
          AppConfig.settings.baseApiUrl = this.environment.baseApiUrl
          AppConfig.settings.authUrl = this.environment.authUrl
          AppConfig.settings.logoutUrl = this.environment.logoutUrl
          return value
        }).catch(reason => {
          AppConfig.settings = this.defaultConfig()
          return reason
        })
    }
    else {
      return new Promise((resolve, reject) => {
        AppConfig.settings = this.defaultConfig()
        resolve(AppConfig.settings)
      })
    }
    }
  }
