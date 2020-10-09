export interface IAppConfig {
  baseApiUrl: string,
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
}

// Default configuration
export class DefaultAppConfig implements IAppConfig {
  baseApiUrl = ""
  editableAuthor = true
  defaultAuthorValue = ""
  toolName = "OSMT"
  toolNameLong = "Open Skills Management Tool"
  publicSkillTitle = "Rich Skill Descriptor"
  publicCollectionTitle = "Rich Skill Descriptor Collection"
  licensePrimary = "© 2020 Western Governors University - WGU."
  licenseSecondary = "All Rights Reserved."
  poweredBy = "Powered by the"
  poweredByUrl = "https://rsd.osmt.dev"
  poweredByLabel = "Open Skills Network"
}
