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
    settings.baseApiUrl = environment.baseApiUrl
    settings.loginUrl = environment.loginUrl
    return settings
  }

  dirname(s: string): string {
    return s.substring(0, s.lastIndexOf('/'))
  }

  relativeFromWhitelabel(whitelabelUrl: string, url?: string): string | undefined {
    if (url) {
      if (url.startsWith("http")) {
        return url
      }

      // if url is not absolute, assume it is a sibling to whitelabel.json
      const wlu = new URL(whitelabelUrl)
      wlu.pathname = `${this.dirname(wlu.pathname)}/${url}`
      return wlu.toString()
    }

    return undefined
  }

  load(): Promise<object> {
    const whitelabelUrl = environment.whiteLabelUrl || `${environment.baseApiUrl}/whitelabel/whitelabel.json`

    if (environment.dynamicWhitelabel) {
      return this.http.get(whitelabelUrl)
        .toPromise()
        .then(value => {
          AppConfig.settings = this.defaultConfig()
          Object.assign(AppConfig.settings, value as IAppConfig)

          AppConfig.settings.siteLogoUrl = this.relativeFromWhitelabel(whitelabelUrl, AppConfig.settings.siteLogoUrl)
          AppConfig.settings.faviconUrl = this.relativeFromWhitelabel(whitelabelUrl, AppConfig.settings.faviconUrl)

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
