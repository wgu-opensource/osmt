import { Component, Inject, Input, LOCALE_ID, OnInit } from "@angular/core"
import { Router } from "@angular/router"

import { Observable } from "rxjs"

import { RichSkillService } from "../../../service/rich-skill.service"
import { SvgHelper, SvgIcon } from "../../../../core/SvgHelper"
import { ExportRsdComponent } from "../../../../export/export-rsd.component"
import { TableActionDefinition } from "../../../../table/skills-library-table/has-action-definitions"
import { ToastService } from "../../../../toast/toast.service"
import {PublishStatus} from "../../../../PublishStatus"

@Component({
  selector: "app-abstract-public-rich-skill-action-bar",
  template: ``
})
export class PublicRichSkillActionBarComponent implements OnInit {

  @Input() skillUuid = ""
  @Input() skillName = ""
  @Input() skillUrl = ""
  @Input() skillStatus?: PublishStatus

  skillJsonObservable = new Observable<string>()
  jsonClipboard = ""

  exporter = new ExportRsdComponent(
    this.richSkillService,
    this.toastService,
    this.locale
  );

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

  onCopyJSON(skillJson: HTMLTextAreaElement): void {
    this.skillJsonObservable.subscribe(() => {
      skillJson.select()
      document.execCommand("copy")
      skillJson.setSelectionRange(0, 0)
      this.toastService.showToast("Success!", "JSON copied to clipboard")
    })
  }

  get action(): TableActionDefinition {
    return new TableActionDefinition({
      label: "Download",
      icon: this.downloadIcon,
      menu: [
        {
          label: "Download as CSV",
          visible: () => true,
          callback: () => this.exporter.getRsdCsv(
            this.skillUuid,
            this.skillName
          ),
        },
        {
          label: "Download as Excel Workbook",
          visible: () => true,
          callback: () => this.exporter.getRsdXlsx(
            this.skillUuid,
            this.skillName,
            this.skillStatus
          ),
        }
      ],
      visible: () => true
    })
  }
}
