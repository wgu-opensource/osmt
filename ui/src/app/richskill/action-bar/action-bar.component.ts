import {Component, Input, OnInit} from "@angular/core"
import {Router} from "@angular/router"
import {AppConfig} from "../../app.config"
import {RichSkillService} from "../service/rich-skill.service"
import * as moment from "moment"

@Component({
  selector: "app-action-bar",
  templateUrl: "./action-bar.component.html"
})
export class ActionBarComponent implements OnInit {

  @Input() skillUuid = ""
  @Input() skillName = ""

  constructor(private router: Router, private richSkillService: RichSkillService) { }

  // This gets mapped to a visually hidden textarea so that javascript has a node to copy from
  href = ""

  ngOnInit(): void {
    this.href = `${AppConfig.settings.baseApiUrl}${this.router.url}`
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
        const date = moment().format("yyyy-mm-DD")
        saveAs(blob, `RSD Collection - ${skillExportName} ${date}.csv`)
      })
  }

  onCopyJSON(): void {
    console.log("You just clicked Copy JSON")
  }

}
