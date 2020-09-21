export interface IAppConfig {
    baseApiUrl: string
}

// Default configuration
export class DefaultAppConfig implements IAppConfig {
  baseApiUrl = ""
}
