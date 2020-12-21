import { Injectable } from "@angular/core"
import {DefaultAppConfig, IAppConfig} from "./models/app-config.model"
import { environment } from "../environments/environment"
import {HttpClient} from "@angular/common/http"

@Injectable()
export class AppConfig {

  static settings: IAppConfig

  constructor(private http: HttpClient) {

  }

  load(): Promise<object> {
    const baseUrl = environment.baseApiUrl
    const loginUrl = environment.loginUrl

    return this.http.get(`${baseUrl}/whitelabel/whitelabel.json`)
      .toPromise()
      .then(value => {
        AppConfig.settings = value as IAppConfig
        AppConfig.settings.baseApiUrl = baseUrl
        AppConfig.settings.loginUrl = loginUrl

        return value
      }).catch(reason => {
        AppConfig.settings = new DefaultAppConfig()
        return reason
      })
  }
}
