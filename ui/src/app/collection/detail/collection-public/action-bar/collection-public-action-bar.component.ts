import {Component, Inject, Input, LOCALE_ID, OnInit} from "@angular/core"
import {CollectionService} from "../../../service/collection.service"
import {Router} from "@angular/router"
import {ToastService} from "../../../../toast/toast.service"
import {SvgHelper, SvgIcon} from "../../../../core/SvgHelper"
import {formatDate} from "@angular/common"
import {ITaskResult} from "../../../../task/ApiTaskResult"
import { Observable } from "rxjs"
import * as FileSaver from "file-saver";

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
  jsonClipboard = ""

  taskUuidInProgress: string | undefined
  csvExport: string | undefined
  intervalHandle: number | undefined

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

  pollCsv(): void {
    if (this.taskUuidInProgress === undefined) { // fail fast
      clearInterval(this.intervalHandle)
      return
    }

    this.collectionService.getCsvTaskResultsIfComplete(this.taskUuidInProgress)
      .subscribe(({body, status}) => {
        if (status === 200) {
          this.csvExport = body as string
          this.taskUuidInProgress = undefined

          clearInterval(this.intervalHandle)

          const blob = new Blob([body], { type: "text/csv;charset=utf-8;" })
          const date = formatDate(new Date(), "yyyy-MM-dd", this.locale)
          FileSaver.saveAs(blob, `RSD Skills - ${this.collectionName} ${date}.csv`)
        }
      })
  }

  onCopyURL(fullPath: HTMLTextAreaElement): void {
    fullPath.select()
    document.execCommand("copy")
    fullPath.setSelectionRange(0, 0)
    this.toastService.showToast("Success!", "URL copied to clipboard")
  }

  onDownloadCsv(): void {
    this.collectionService.requestCollectionSkillsCsv(this.collectionUuid)
      .subscribe((taskStarted: ITaskResult) => {
        this.taskUuidInProgress = taskStarted.uuid
        this.intervalHandle = setInterval(() => this.pollCsv(), 1000)
      })
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
