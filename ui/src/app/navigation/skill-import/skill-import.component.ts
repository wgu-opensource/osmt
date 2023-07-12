import { Component } from '@angular/core';
import { ActivatedRoute, Router } from "@angular/router";
import { ButtonAction } from "../../auth/auth-roles";
import { AuthService } from "../../auth/auth-service";
import { CollectionService } from "../../collection/service/collection.service";
import { TableActionDefinition } from "../../table/skills-library-table/has-action-definitions";
import { BatchImportOptionsEnum } from "../../richskill/import/BatchImportOptionsEnum";

@Component({
  selector: 'app-skill-import',
  templateUrl: './skill-import.component.html',
  styleUrls: [
    '../../table/skills-library-table/action-bar-item.components.scss',
    './skill-import.component.scss'
  ]
})
export class SkillImportComponent {

  constructor(protected collectionService: CollectionService,
              protected route: ActivatedRoute,
              protected  authService: AuthService,
              protected router: Router
  ) {
    this.setEnableFlags()
  }

  canSkillUpdate: boolean = false
  canSkillCreate: boolean = false
  canSkillPublish: boolean = false
  canCollectionUpdate: boolean = false
  canCollectionCreate: boolean = false
  canCollectionPublish: boolean = false
  canCollectionSkillsUpdate: boolean = false

  setEnableFlags(): void {
    this.canSkillUpdate = this.authService.isEnabledByRoles(ButtonAction.SkillUpdate);
    this.canSkillCreate = this.authService.isEnabledByRoles(ButtonAction.SkillCreate);
    this.canSkillPublish = this.authService.isEnabledByRoles(ButtonAction.SkillPublish);
    this.canCollectionUpdate = this.authService.isEnabledByRoles(ButtonAction.CollectionUpdate);
    this.canCollectionCreate = this.authService.isEnabledByRoles(ButtonAction.CollectionCreate);
    this.canCollectionPublish = this.authService.isEnabledByRoles(ButtonAction.CollectionPublish);
    this.canCollectionSkillsUpdate = this.authService.isEnabledByRoles(ButtonAction.CollectionSkillsUpdate);
  }

  get action(): TableActionDefinition {
    return new TableActionDefinition({
      menu: [
        {
          label: "Import to Existing Collection",
          visible: () => true,
          callback: () => {
            this.router.navigate(["/skills/import"],
            {queryParams:{to:BatchImportOptionsEnum.existing}})
          },
        },
        {
          label: "Import to Workspace",
          visible: () => true,
          callback: () => {
            this.router.navigate(["/skills/import"],
              {queryParams: {to: BatchImportOptionsEnum.workspace}})
          },
        },
        {
          label: "Import to New Collection",
          visible: () => true,
          callback: () => {
            this.router.navigate(["/skills/import"],
            {queryParams: {to: BatchImportOptionsEnum.new}})
          },
        }
      ],
      visible: () => true
    })
  }
}
