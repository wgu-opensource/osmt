import {Router} from "@angular/router"
import {RichSkillService} from "../service/rich-skill.service"
import {ToastService} from "../../toast/toast.service"
import {formatDate} from "@angular/common"
import {OnInit} from "@angular/core"
import {AppConfig} from "../../app.config"

export abstract class RichSkillActionBar implements OnInit {

  abstract skillUuid: string
  abstract skillName: string

  // Used in invisible labels to house the data to be added to clipboard
  abstract href: string
  abstract jsonClipboard: string

  constructor(
    protected router: Router,
    protected richSkillService: RichSkillService,
    protected toastService: ToastService,
    protected locale: string
  ) {
  }

  ngOnInit(): void {
    this.href = `${AppConfig.settings.baseApiUrl}${this.router.url}`
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
        const date = formatDate(new Date(), "yyyy-mm-dd", this.locale)
        saveAs(blob, `RSD Collection - ${skillExportName} ${date}.csv`)
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
