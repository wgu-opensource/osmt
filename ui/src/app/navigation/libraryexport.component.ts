import {Component, OnInit, LOCALE_ID, Inject} from "@angular/core"
import {formatDate} from "@angular/common"
import {SearchService} from "../search/search.service"
import {RichSkillService} from "../richskill/service/rich-skill.service"
import {ActivatedRoute} from "@angular/router"
import {AuthService} from "../auth/auth-service"
import * as FileSaver from "file-saver"
import {SvgHelper, SvgIcon} from "../core/SvgHelper"
import {AbstractSearchComponent} from "./abstract-search.component"
import {ApiTaskResult} from "../task/ApiTaskResult"
import {Observable} from "rxjs"

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
      .subscribe((response: ApiTaskResult) => {
        this.richSkillService.getResultExportedLibrary(response.id.slice(1)).subscribe(
          csv => {
            console.log(csv.body)
            const blob = new Blob([csv.body], {type: "text/csv;charset=utf-8;"})
            const date = formatDate(new Date(), "yyyy-MM-dd", this.locale)
            FileSaver.saveAs(blob, `RSD Library - OSMT ${date}.csv`)
          }
        )
      })
  }

}
