export enum ButtonAction {
  SkillUpdate,
  SkillCreate,
  SkillPublish,
  CollectionUpdate,
  CollectionCreate,
  CollectionPublish,
  CollectionSkillsUpdate
}

//TODO migrate  AuthServiceWgu & AuthService.hasRole & isEnabledByRoles into a singleton here. HDN Sept 15, 2022

export interface AuthRolesConfig {
  isEnabled: boolean,
  admin_role: string,
  curator_role: string
}

export class AuthRoles {
  public static instance = new AuthRoles()
  private config: AuthRolesConfig = {
    isEnabled: false,
    admin_role: "",
    curator_role: ""
  }
  private mapping = new Map<ButtonAction, string[]>([])

  constructor() {
  }

  /** Call this function to set the AuthRole config. No defaults. */
  init(config: AuthRolesConfig) {
    this.config = config

    this.updateRoleMapping()
  }

  get isEnabled(): boolean {
    return this.config.isEnabled
  }

  get admin_role(): string {
    return this.config.admin_role;
  }

  get curator_role(): string {
    return this.config.curator_role;
  }

  /** Get the roles required by the action. */
  rolesByAction(action: ButtonAction): string[] | undefined {
    return this.mapping.get(action)
  }

  /** Update the mapping based on the configured roles. */
  private updateRoleMapping() {
    this.mapping = new Map<ButtonAction, string[]>([
      [ButtonAction.SkillUpdate,            [this.admin_role, this.curator_role]],
      [ButtonAction.SkillCreate,            [this.admin_role, this.curator_role]],
      [ButtonAction.SkillPublish,           [this.admin_role]],
      [ButtonAction.CollectionUpdate,       [this.admin_role, this.curator_role]],
      [ButtonAction.CollectionCreate,       [this.admin_role, this.curator_role]],
      [ButtonAction.CollectionPublish,      [this.admin_role]],
      [ButtonAction.CollectionSkillsUpdate, [this.admin_role]],
    ])
  }
}
