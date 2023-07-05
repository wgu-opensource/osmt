import { Component } from '@angular/core';
import {ButtonAction} from "../../auth/auth-roles";
import {ActivatedRoute} from "@angular/router";
import {AuthService} from "../../auth/auth-service";
import {CollectionService} from "../../collection/service/collection.service";
import {TableActionDefinition} from "../../table/skills-library-table/has-action-definitions";

@Component({
  selector: 'app-skill-import',
  templateUrl: './skill-import.component.html',
  styleUrls: ['./skill-import.component.scss']
})
export class SkillImportComponent {

  constructor(protected collectionService: CollectionService, protected route: ActivatedRoute, protected  authService: AuthService) {
    this.setEnableFlags()
  }

  canSkillUpdate: boolean = false
  canSkillCreate: boolean = false
  canSkillPublish: boolean = false
  canCollectionUpdate: boolean = false
  canCollectionCreate: boolean = false
  canCollectionPublish: boolean = false
  canCollectionSkillsUpdate: boolean = false
  canExportLibrary: boolean = false

  setEnableFlags(): void {
    this.canSkillUpdate = this.authService.isEnabledByRoles(ButtonAction.SkillUpdate);
    this.canSkillCreate = this.authService.isEnabledByRoles(ButtonAction.SkillCreate);
    this.canSkillPublish = this.authService.isEnabledByRoles(ButtonAction.SkillPublish);
    this.canCollectionUpdate = this.authService.isEnabledByRoles(ButtonAction.CollectionUpdate);
    this.canCollectionCreate = this.authService.isEnabledByRoles(ButtonAction.CollectionCreate);
    this.canCollectionPublish = this.authService.isEnabledByRoles(ButtonAction.CollectionPublish);
    this.canCollectionSkillsUpdate = this.authService.isEnabledByRoles(ButtonAction.CollectionSkillsUpdate);
    this.canExportLibrary = this.authService.isEnabledByRoles(ButtonAction.LibraryExport);
  }

  get action(): TableActionDefinition {
    return new TableActionDefinition({
      menu: [
        {
          label: "Download as CSV",
          visible: () => true,
          callback: () => this.exporter.exportLibraryCsv(),
        },
        {
          label: "Download as Excel Workbook",
          visible: () => true,
          callback: () => this.exporter.exportLibraryXlsx(),
        }
      ],
      visible: () => true
    })
  }

}
