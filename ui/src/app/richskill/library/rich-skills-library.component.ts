import {Component, Inject, LOCALE_ID, OnInit} from "@angular/core"
import {RichSkillService} from "../service/rich-skill.service"
import {SkillsListComponent} from "../list/skills-list.component"
import {ToastService} from "../../toast/toast.service"
import {PaginatedSkills} from "../service/rich-skill-search.service"
import {Router} from "@angular/router"
import {determineFilters} from "../../PublishStatus"
import {Title} from "@angular/platform-browser"
import {AuthService} from "../../auth/auth-service"
import {formatDate} from "@angular/common"
import * as FileSaver from "file-saver"

@Component({
  selector: "app-rich-skills-library",
  templateUrl: "../list/skills-list.component.html"
})
export class RichSkillsLibraryComponent extends SkillsListComponent implements OnInit {

  title = "RSD Library"

  constructor(
    protected router: Router,
    protected richSkillService: RichSkillService,
    protected toastService: ToastService,
    protected titleService: Title,
    protected authService: AuthService,
    @Inject(LOCALE_ID) protected locale: string
  ) {
    super(router, richSkillService, toastService, authService)
  }

  ngOnInit(): void {
    this.titleService.setTitle(`${this.title} | ${this.whitelabel.toolName}`)
    this.loadNextPage()
  }

  loadNextPage(): void {
    if (this.selectedFilters.size < 1) {
      this.setResults(new PaginatedSkills([], 0))
      return
    }

    this.resultsLoaded = this.richSkillService.getSkills(this.size, this.from, determineFilters(this.selectedFilters), this.columnSort)
    this.resultsLoaded.subscribe((results) => {
      this.setResults(results)
    })
  }

  getSelectAllEnabled(): boolean {
    return false
  }

  protected handleClickExportSearch(): void {
    this.toastService.loaderSubject.next(true)
    this.richSkillService.exportSearch(this.selectedUuids() as string[])
      .subscribe((apiTask) => {
        this.richSkillService.getResultExportedLibrary(apiTask.id.slice(1)).subscribe(
          response => {
            this.downloadAsCsvFile(response.body)
            this.toastService.loaderSubject.next(false)
          }
        )
      })
  }

  private downloadAsCsvFile(csv: string): void {
    const blob = new Blob([csv], {type: "text/csv;charset=utf-8;"})
    const date = formatDate(new Date(), "yyyy-MM-dd", this.locale)
    FileSaver.saveAs(blob, `RSD Library - OSMT ${date}.csv`)
  }

}
