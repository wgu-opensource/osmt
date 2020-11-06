import {Router} from "@angular/router"
import {RichSkillService} from "../../../service/rich-skill.service"
import {ToastService} from "../../../../toast/toast.service"
import {formatDate} from "@angular/common"
import {Component, Inject, LOCALE_ID, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../../core/SvgHelper"
import {saveAs} from "file-saver"

@Component({template: ""})
export abstract class PublicRichSkillActionBarComponent implements OnInit {

  abstract skillUuid: string
  abstract skillName: string
  abstract skillUrl: string

  abstract jsonClipboard: string

  duplicateIcon = SvgHelper.path(SvgIcon.DUPLICATE)
  downloadIcon = SvgHelper.path(SvgIcon.DOWNLOAD)
  dismissIcon = SvgHelper.path(SvgIcon.DISMISS)

  constructor(
    protected router: Router,
    protected richSkillService: RichSkillService,
    protected toastService: ToastService,
    @Inject(LOCALE_ID) private locale: string
  ) {
  }

  ngOnInit(): void {
    this.richSkillService.getSkillJsonByUuid(this.skillUuid)
      .subscribe( (json: string) => this.jsonClipboard = json)
  }

  onCopyURL(fullPath: HTMLTextAreaElement): void {
    console.log(`copying ${fullPath.textContent}`)
    fullPath.select()
    document.execCommand("copy")
    fullPath.setSelectionRange(0, 0)
  }

  onDownloadCsv(): void {
    const skillExportName = this.skillName ? this.skillName : "OSMT Skill"
    this.richSkillService.getSkillCsvByUuid(this.skillUuid)
      .subscribe((csv: string) => {
        const blob = new Blob([csv], {type: "text/csv;charset=utf-8;"})
        const date = formatDate(new Date(), "yyyy-MM-dd", this.locale)
        saveAs(blob, `RSD Skill - ${skillExportName} ${date}.csv`)
      })
  }

  onCopyJSON(skillJson: HTMLTextAreaElement): void {
    this.richSkillService.getSkillJsonByUuid(this.skillUuid)
      .subscribe((json: string) => {
        this.jsonClipboard = json
        console.log(`copying ${skillJson.textContent}`)
        skillJson.select()
        document.execCommand("copy")
        skillJson.setSelectionRange(0, 0)
        this.toastService.showToast("Copied", "JSON was successfully copied to the clipboard")
      })
  }
}
