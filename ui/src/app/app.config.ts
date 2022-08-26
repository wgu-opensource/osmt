import { Injectable } from "@angular/core"
import {DefaultAppConfig, IAppConfig} from "./models/app-config.model"
import { environment } from "../environments/environment"
import {HttpClient} from "@angular/common/http"

@Injectable()
export class AppConfig {

  static settings: IAppConfig
  environment: { production: boolean; baseApiUrl: string; loginUrl: string; dynamicWhitelabel: boolean }

  constructor(
    private http: HttpClient
  ) {
    this.environment = environment
  }

  defaultConfig(): IAppConfig {
    const settings = new DefaultAppConfig()
    settings.baseApiUrl = this.environment.baseApiUrl
    settings.loginUrl = this.environment.loginUrl
    return settings
  }

  load(): Promise<object> {
    const baseUrl = this.environment.baseApiUrl

    if (this.environment.dynamicWhitelabel) {
      return this.http.get(`${baseUrl}/whitelabel/whitelabel.json`)
        .toPromise()
        .then(value => {
          AppConfig.settings = this.defaultConfig()
          Object.assign(AppConfig.settings, value as IAppConfig)

          // baseApiUrl and loginUrl are not runtime whitelabellable
          AppConfig.settings.baseApiUrl = this.environment.baseApiUrl
          AppConfig.settings.loginUrl = this.environment.loginUrl
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
