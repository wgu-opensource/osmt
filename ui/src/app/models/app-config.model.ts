export interface IAppConfig {
    baseApiUrl: string,
    editableAuthor: boolean,
    defaultAuthorValue: string
}

// Default configuration
export class DefaultAppConfig implements IAppConfig {
  baseApiUrl = ""
  editableAuthor = true
  defaultAuthorValue = ""
}
