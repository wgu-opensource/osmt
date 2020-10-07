import {IAppConfig} from "./app/models/app-config.model";
import {AppConfig} from "./app/app.config";

export class Whitelabelled {
  get whitelabel(): IAppConfig {
    return AppConfig.settings
  }
}


