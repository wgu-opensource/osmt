import {Component, OnInit, LOCALE_ID, Inject} from "@angular/core"
import {formatDate} from "@angular/common"
import {SearchService} from "../search/search.service"
import {RichSkillService} from "../richskill/service/rich-skill.service"
import {ActivatedRoute} from "@angular/router"
import {AuthService} from "../auth/auth-service"
import * as FileSaver from "file-saver"
import {SvgHelper, SvgIcon} from "../core/SvgHelper"
import {AbstractSearchComponent} from "./abstract-search.component"

@Component({
  selector: "app-libraryexport",
  templateUrl: "./libraryexport.component.html"
})
export class LibraryExportComponent extends AbstractSearchComponent implements OnInit {

  searchIcon = SvgHelper.path(SvgIcon.SEARCH)
  dismissIcon = SvgHelper.path(SvgIcon.DISMISS)

  constructor(
    protected searchService: SearchService,
    protected route: ActivatedRoute,
    protected authService: AuthService,
    protected richSkillService: RichSkillService,
    @Inject(LOCALE_ID) protected locale: string) {
    super(searchService, route, authService)
  }

  ngOnInit(): void {
  }

  onDownloadLibrary(): void {
    this.richSkillService.libraryExport()
      .subscribe((csv: string) => {
        const blob = new Blob([csv], {type: "text/csv;charset=utf-8;"})
        const date = formatDate(new Date(), "yyyy-MM-dd", this.locale)
        FileSaver.saveAs(blob, `RSD Library - OSMT ${date}.csv`)
      })
  }
}
