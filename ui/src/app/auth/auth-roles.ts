export const OSMT_ADMIN = "ROLE_Osmt_Admin"
export const OSMT_CURATOR = "ROLE_Osmt_Curator"

export const ENABLE_ROLES = false

export enum ButtonAction {
  SkillUpdate,
  SkillCreate,
  SkillPublish,
  CollectionUpdate,
  CollectionCreate,
  CollectionPublish,
  CollectionSkillsUpdate
}

export const ActionByRoles = new Map<number, string[]>([
  [ButtonAction.SkillUpdate,            [OSMT_ADMIN, OSMT_CURATOR]],
  [ButtonAction.SkillCreate,            [OSMT_ADMIN, OSMT_CURATOR]],
  [ButtonAction.SkillPublish,           [OSMT_ADMIN]],
  [ButtonAction.CollectionUpdate,       [OSMT_ADMIN, OSMT_CURATOR]],
  [ButtonAction.CollectionCreate,       [OSMT_ADMIN, OSMT_CURATOR]],
  [ButtonAction.CollectionPublish,      [OSMT_ADMIN]],
  [ButtonAction.CollectionSkillsUpdate, [OSMT_ADMIN]],
]);

