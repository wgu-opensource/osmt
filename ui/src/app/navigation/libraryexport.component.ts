import { Component, OnInit, LOCALE_ID, Inject } from "@angular/core"
import { ActivatedRoute } from "@angular/router"

import { AbstractSearchComponent } from "./abstract-search.component"
import { AuthService } from "../auth/auth-service"
import { SvgHelper, SvgIcon } from "../core/SvgHelper"
import { ExportRsdComponent } from "../export/export-rsd.component"
import { RichSkillService } from "../richskill/service/rich-skill.service"
import { SearchService } from "../search/search.service"
import { TableActionDefinition } from "../table/skills-library-table/has-action-definitions"
import { ToastService } from "../toast/toast.service"

@Component({
  selector: "app-libraryexport",
  templateUrl: "./libraryexport.component.html",
  styleUrls: [
    "../table/skills-library-table/action-bar-item.components.scss",
    "./libraryexport.component.scss",
  ]
})
export class LibraryExportComponent extends AbstractSearchComponent implements OnInit {

  searchIcon = SvgHelper.path(SvgIcon.SEARCH)
  dismissIcon = SvgHelper.path(SvgIcon.DISMISS)

  exporter = new ExportRsdComponent(
    this.richSkillService,
    this.toastService,
    this.locale
  );

  constructor(
    protected searchService: SearchService,
    protected route: ActivatedRoute,
    protected authService: AuthService,
    protected richSkillService: RichSkillService,
    @Inject(LOCALE_ID) protected locale: string,
    private toastService: ToastService
  ) {
    super(searchService, route, authService)
  }

  ngOnInit(): void {
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
