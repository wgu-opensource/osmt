export interface AuthProvider {
  id: string;
  name: string;
  authorizationUrl: string;
}

export interface IAppConfig {
  baseApiUrl: string;
  loginUrl: string;
  authMode?: string;
  authProviders?: AuthProvider[];
  singleAuthEnabled?: boolean;
  editableAuthor: boolean;
  defaultAuthorValue: string;
  toolName: string;
  toolNameLong: string;
  publicSkillTitle: string;
  publicCollectionTitle: string;
  licensePrimary: string;
  licenseSecondary: string;
  poweredBy: string;
  poweredByUrl: string;
  poweredByLabel: string;
  idleTimeoutInSeconds: number;
  colorBrandAccent1?: string;
  logoUrl?: string;
  readOnlyMode?: boolean;
  publicInstanceUrl?: string;
  authoringWelcomeMessage?: string;
}

// Default configuration
export class DefaultAppConfig implements IAppConfig {
  baseApiUrl = '';
  loginUrl = '';
  editableAuthor = true;
  defaultAuthorValue = '';
  toolName = 'OSMT';
  toolNameLong = 'Open Skills Management Tool';
  publicSkillTitle = 'Rich Skill Descriptor';
  publicCollectionTitle = 'Rich Skill Descriptor Collection';
  licensePrimary = 'Copyright © OSMT Contributors';
  licenseSecondary = 'All rights reserved.';
  poweredBy = '';
  poweredByUrl = '';
  poweredByLabel = '';
  idleTimeoutInSeconds = 24 * 60 * 60;
  colorBrandAccent1 = undefined;
  /** Light-colored mark for the brand-colored navbar (see header). */
  logoUrl = '/assets/images/logo-light.svg';
  dynamicWhitelabel = false;
  authProviders: AuthProvider[] = [];
  singleAuthEnabled = false;
  readOnlyMode = false;
  publicInstanceUrl = '';
  authoringWelcomeMessage = '';
}
