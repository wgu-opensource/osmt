import {Router} from "@angular/router"
import {RichSkillService} from "../../../service/rich-skill.service"
import {ToastService} from "../../../../toast/toast.service"
import {formatDate} from "@angular/common"
import {Component, Inject, Input, LOCALE_ID, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../../core/SvgHelper"
import * as FileSaver from "file-saver"
import {Observable} from "rxjs"

@Component({
  selector: "app-abstract-public-rich-skill-action-bar",
  template: ``
})
export class PublicRichSkillActionBarComponent implements OnInit {

  @Input() skillUuid = ""
  @Input() skillName = ""
  @Input() skillUrl = ""

  skillJsonObservable = new Observable<string>()
  jsonClipboard = ""

  duplicateIcon = SvgHelper.path(SvgIcon.DUPLICATE)
  downloadIcon = SvgHelper.path(SvgIcon.DOWNLOAD)
  dismissIcon = SvgHelper.path(SvgIcon.DISMISS)

  constructor(
    protected router: Router,
    protected richSkillService: RichSkillService,
    protected toastService: ToastService,
    @Inject(LOCALE_ID) protected locale: string
  ) {
  }

  ngOnInit(): void {
    this.skillJsonObservable = this.richSkillService.getSkillJsonByUuid(this.skillUuid)
    this.skillJsonObservable.subscribe( (json: string) => this.jsonClipboard = json)
  }

  onCopyURL(fullPath: HTMLTextAreaElement): void {
    fullPath.select()
    document.execCommand("copy")
    fullPath.setSelectionRange(0, 0)
    this.toastService.showToast("Success!", "URL copied to clipboard")
  }

  onDownloadCsv(): void {
    const skillExportName = this.skillName ? this.skillName : "OSMT Skill"
    this.richSkillService.getSkillCsvByUuid(this.skillUuid)
      .subscribe((csv: string) => {
        const blob = new Blob([csv], {type: "text/csv;charset=utf-8;"})
        const date = formatDate(new Date(), "yyyy-MM-dd", this.locale)
        FileSaver.saveAs(blob, `RSD Skill - ${skillExportName} ${date}.csv`)
      })
  }

  onCopyJSON(skillJson: HTMLTextAreaElement): void {
    this.skillJsonObservable.subscribe(() => {
      skillJson.select()
      document.execCommand("copy")
      skillJson.setSelectionRange(0, 0)
      this.toastService.showToast("Success!", "JSON copied to clipboard")
    })
  }
}
