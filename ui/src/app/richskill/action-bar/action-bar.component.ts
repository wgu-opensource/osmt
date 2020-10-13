import {Component, Inject, Input, LOCALE_ID, OnInit} from "@angular/core"
import {Router} from "@angular/router"
import {AppConfig} from "../../app.config"
import {RichSkillService} from "../service/rich-skill.service"
import {ToastService} from "../../toast/toast.service";
import {formatDate} from "@angular/common";

@Component({
  selector: "app-action-bar",
  templateUrl: "./action-bar.component.html"
})
export class ActionBarComponent implements OnInit {

  @Input() skillUuid = ""
  @Input() skillName = ""

  constructor(
    private router: Router,
    private richSkillService: RichSkillService,
    private toastService: ToastService,
    @Inject(LOCALE_ID) public locale: string
  ) {
  }

  // This gets mapped to a visually hidden textarea so that javascript has a node to copy from
  href = ""
  jsonClipboard = ""

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
