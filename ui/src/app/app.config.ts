import { Injectable } from "@angular/core"
import {DefaultAppConfig, IAppConfig} from "./models/app-config.model"
import { environment } from "../environments/environment"
import {HttpClient} from "@angular/common/http"

@Injectable()
export class AppConfig {

  static settings: IAppConfig

  constructor(private http: HttpClient) {

  }

  defaultConfig(): IAppConfig {
    const settings = new DefaultAppConfig()
    settings.production = environment.production
    settings.baseApiUrl = environment.baseApiUrl
    settings.loginUrl = environment.loginUrl
    settings.isAuthEnabled = environment.isAuthEnabled
    return settings
  }

  load(): Promise<object> {
    const baseUrl = environment.baseApiUrl

    if (environment.dynamicWhitelabel) {
      return this.http.get(`${baseUrl}/whitelabel/whitelabel.json`)
        .toPromise()
        .then(value => {
          AppConfig.settings = this.defaultConfig()
          Object.assign(AppConfig.settings, value as IAppConfig)

          // baseApiUrl and loginUrl are not runtime whitelabellable
          AppConfig.settings.baseApiUrl = environment.baseApiUrl
          AppConfig.settings.loginUrl = environment.loginUrl
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
