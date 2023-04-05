import { Component, Inject, Input, LOCALE_ID, OnInit } from "@angular/core"
import { Router } from "@angular/router"

import { Observable } from "rxjs"

import { SvgHelper, SvgIcon } from "../../../../core/SvgHelper"
import { ExportCollectionComponent } from "../../../../export/export-collection.component"
import { CollectionService } from "../../../service/collection.service"
import { TableActionDefinition } from "../../../../table/skills-library-table/has-action-definitions"
import { ToastService } from "../../../../toast/toast.service"


@Component({
  selector: "app-collection-public-action-bar",
  template: ``
})
export class CollectionPublicActionBarComponent implements OnInit {

  @Input() collectionUrl = ""
  @Input() collectionUuid = ""
  @Input() collectionName = ""

  duplicateIcon = SvgHelper.path(SvgIcon.DUPLICATE)
  downloadIcon = SvgHelper.path(SvgIcon.DOWNLOAD)

  collectionJsonObservable = new Observable<string>()
  exporter = new ExportCollectionComponent(
    this.collectionService,
    this.toastService,
    this.locale
  )
  jsonClipboard = ""

  action = new TableActionDefinition({
    label: "Download",
    icon: this.downloadIcon,
    menu: [
      {
        label: "Download as CSV",
        visible: () => true,
        callback: () => this.exporter.getCollectionCsv(
          this.collectionUuid,
          this.collectionName
        )
      },
      {
        label: "Download as Excel Workbook",
        visible: () => true,
        callback: () => this.exporter.getCollectionXlsx(
          this.collectionUuid,
          this.collectionName
        )
      }
    ],
    visible: () => true
  })

  constructor(
    protected router: Router,
    protected collectionService: CollectionService,
    protected toastService: ToastService,
    @Inject(LOCALE_ID) protected locale: string
  ) {}

  ngOnInit(): void {
    if (this.collectionUuid) {
      this.collectionJsonObservable = this.collectionService.getCollectionJson(this.collectionUuid)
      this.collectionJsonObservable.subscribe( (json: string) => {
        this.jsonClipboard = json
      })
    }
  }

  onCopyURL(fullPath: HTMLTextAreaElement): void {
    fullPath.select()
    document.execCommand("copy")
    fullPath.setSelectionRange(0, 0)
    this.toastService.showToast("Success!", "URL copied to clipboard")
  }

  onCopyJSON(collectionJson: HTMLTextAreaElement): void {
    this.collectionJsonObservable.subscribe(() => {
      collectionJson.select()
      document.execCommand("copy")
      collectionJson.setSelectionRange(0, 0)
      this.toastService.showToast("Success!", "JSON copied to clipboard")
    })
  }

}
