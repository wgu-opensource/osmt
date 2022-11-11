import {Component, OnInit, LOCALE_ID, Inject} from "@angular/core"
import {formatDate} from "@angular/common"
import {AbstractSearchComponent} from "./abstract-search.component"
import {SearchService} from "../search/search.service"
import {RichSkillService} from "../richskill/service/rich-skill.service"
import {ActivatedRoute} from "@angular/router"
import {SvgHelper, SvgIcon} from "../core/SvgHelper"
import {AuthService} from "../auth/auth-service"
import * as FileSaver from "file-saver"

@Component({
  selector: "app-commoncontrols",
  templateUrl: "./commoncontrols.component.html"
})
export class CommoncontrolsComponent extends AbstractSearchComponent implements OnInit {

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
  /**
  * Added the logic for export library here because this controllers template holds the button
  * The other links on the template link out to other templates and controllers while the export
  * button required us to have a click action and call a method from this controller. We could possibly
  * move it to the abstract search component or a helper since there are other onDownloadCsv functions
  * that are similar to this
  **/
  onDownloadCsv(): void {
    this.richSkillService.libraryExport()
      .subscribe((csv: string) => {
        const blob = new Blob([csv], {type: "text/csv;charset=utf-8;"})
        const date = formatDate(new Date(), "yyyy-MM-dd", this.locale)
        FileSaver.saveAs(blob, `RSD Library - OSMT ${date}.csv`)
      })
  }
}
