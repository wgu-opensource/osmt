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
    const configUri = environment.whitelabelConfigUri

    if (configUri) {
      return new Promise<void>((resolve, reject) => {
        this.http.get(configUri).toPromise().then(response => {
          AppConfig.settings = (response as IAppConfig)
          resolve()
        }).catch((response) => {
          reject(`Could not load whitelabel config file at '${configUri}': ${JSON.stringify(response)}`)
        })
      })
    } else { // If empty, load our default
      return new Promise<void>( (resolve) => {
        AppConfig.settings = new DefaultAppConfig()
        resolve()
      })
    }
  }
}
