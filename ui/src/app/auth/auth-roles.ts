export const OSMT_ADMIN = "ROLE_Osmt_Admin"
export const OSMT_CURATOR = "ROLE_Osmt_Curator"

export const ENABLE_ROLES = true

export enum ButtonAction {
  SkillUpdate,
  SkillCreate,
  SkillPublish,
  CollectionUpdate,
  CollectionCreate,
  CollectionPublish,
  CollectionSkillsUpdate,
  LibraryExport,
  ExportDraftCollection,
  DeleteCollection,
  MyWorkspace,
  Metadata
}

export const ActionByRoles = new Map<number, string[]>([
  [ButtonAction.SkillUpdate,            [OSMT_ADMIN, OSMT_CURATOR]],
  [ButtonAction.SkillCreate,            [OSMT_ADMIN, OSMT_CURATOR]],
  [ButtonAction.SkillPublish,           [OSMT_ADMIN]],
  [ButtonAction.CollectionUpdate,       [OSMT_ADMIN, OSMT_CURATOR]],
  [ButtonAction.CollectionCreate,       [OSMT_ADMIN, OSMT_CURATOR]],
  [ButtonAction.CollectionPublish,      [OSMT_ADMIN]],
  [ButtonAction.CollectionSkillsUpdate, [OSMT_ADMIN, OSMT_CURATOR]],
  [ButtonAction.LibraryExport,          [OSMT_ADMIN]],
  [ButtonAction.ExportDraftCollection,  [OSMT_ADMIN]],
  [ButtonAction.DeleteCollection,       [OSMT_ADMIN]],
  [ButtonAction.MyWorkspace,            [OSMT_ADMIN, OSMT_CURATOR]],
  [ButtonAction.Metadata,               [OSMT_ADMIN]],
])

//TODO migrate  AuthServiceWgu & AuthService.hasRole & isEnabledByRoles into a singleton here. HDN Sept 15, 2022
