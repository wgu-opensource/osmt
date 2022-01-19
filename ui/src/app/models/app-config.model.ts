export interface IAppConfig {
  baseApiUrl: string,
  loginUrl: string,
  isAuthEnabled: boolean,
  production: boolean,
  editableAuthor: boolean,
  defaultAuthorValue: string,
  toolName: string,
  toolNameLong: string,
  publicSkillTitle: string,
  publicCollectionTitle: string,
  licensePrimary: string
  licenseSecondary: string
  poweredBy: string
  poweredByUrl: string
  poweredByLabel: string
  idleTimeoutInSeconds: number
  colorBrandAccent1?: string
}

// Default configuration
export class DefaultAppConfig implements IAppConfig {
  baseApiUrl = ""
  loginUrl = ""
  isAuthEnabled = true
  production = false
  editableAuthor = true
  defaultAuthorValue = ""
  toolName = "OSMT"
  toolNameLong = "Open Skills Management Tool"
  publicSkillTitle = "Rich Skill Descriptor"
  publicCollectionTitle = "Rich Skill Descriptor Collection"
  licensePrimary = "Copyright © Open Skills Network"
  licenseSecondary = "All rights reserved."
  poweredBy = "Powered by the"
  poweredByUrl = "https://rsd.osmt.dev"
  poweredByLabel = "Open Skills Network"
  idleTimeoutInSeconds = 15 * 60
  colorBrandAccent1 = undefined
  dynamicWhitelabel = false
}
