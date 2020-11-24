import {Component, Input, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "./SvgHelper"
import {PublishStatus} from "../PublishStatus"

@Component({
  selector: "app-status-bar",
  templateUrl: "./status-bar.component.html"
})
export class StatusBarComponent implements OnInit {

  @Input() status: PublishStatus = PublishStatus.Draft
  @Input() publishDate = ""
  @Input() archiveDate = ""
  @Input() showDates = true

  iconUp = SvgHelper.path(SvgIcon.ICON_UP)
  iconArchive = SvgHelper.path(SvgIcon.ARCHIVE)

  constructor() { }

  ngOnInit(): void {
  }

  isPublished(): boolean {
    return !!this.publishDate || this.status === PublishStatus.Published
  }

  isArchived(): boolean {
    return !!this.archiveDate || this.status === PublishStatus.Archived
  }

  isDraft(): boolean {
    return !this.isPublished()
  }
}
