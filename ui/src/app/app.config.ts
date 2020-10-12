import { Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import { DefaultAppConfig, IAppConfig } from "./models/app-config.model"
import { environment } from "../environments/environment"

@Injectable()
export class AppConfig {

  static settings: IAppConfig

  constructor(private http: HttpClient) {

  }

  load(): Promise<void> {
    return new Promise<void>( (resolve) => {
      AppConfig.settings = new DefaultAppConfig()
      AppConfig.settings.baseApiUrl = environment.baseApiUrl
      AppConfig.settings.loginUrl = environment.loginUrl
      resolve()
    })

  }
}
