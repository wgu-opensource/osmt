import { IAppConfig } from './app/models/app-config.model';
import { AppConfig } from './app/app.config';

export class Whitelabelled {
  get whitelabel(): IAppConfig {
    return AppConfig.settings;
  }

  /** True when this is the public read-only instance (split deployment). */
  get isReadOnlyPublicInstance(): boolean {
    return AppConfig.settings.readOnlyMode === true;
  }
}
