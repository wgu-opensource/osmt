import {Component, Inject, Input, LOCALE_ID, OnInit} from "@angular/core"
import {CollectionService} from "../../../service/collection.service"
import {Router} from "@angular/router"
import {ToastService} from "../../../../toast/toast.service"
import {SvgHelper, SvgIcon} from "../../../../core/SvgHelper";

@Component({
  selector: "app-collection-public-action-bar",
  template: ``
})
export class CollectionPublicActionBarComponent implements OnInit {

  @Input() collectionUrl = ""
  @Input() collectionUuid = ""

  duplicateIcon = SvgHelper.path(SvgIcon.DUPLICATE)
  downloadIcon = SvgHelper.path(SvgIcon.DOWNLOAD)

  jsonClipboard = ""

  constructor(
    protected router: Router,
    protected collectionService: CollectionService,
    protected toastService: ToastService,
    @Inject(LOCALE_ID) protected locale: string
  ) {
  }

  ngOnInit(): void {
  }

  onCopyURL(fullPath: HTMLTextAreaElement): void {
    fullPath.select()
    document.execCommand("copy")
    fullPath.setSelectionRange(0, 0)
    this.toastService.showToast("Success!", "URL copied to clipboard")
  }

  onDownloadCsv(): void {
    // TODO
  }

  onCopyJSON(collectonJson: HTMLTextAreaElement): void {
    this.collectionService.getCollectionJson(this.collectionUuid)
      .subscribe((json) => {
        this.jsonClipboard = json
        collectonJson.select()
        document.execCommand("copy")
        collectonJson.setSelectionRange(0, 0)
        this.toastService.showToast("Success!", "JSON copied to clipboard")
      })
  }

}
