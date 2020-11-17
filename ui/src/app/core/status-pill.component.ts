import {Component, Input, OnInit} from "@angular/core"
import {PublishStatus} from "../PublishStatus"
import {SvgHelper, SvgIcon} from "./SvgHelper"

@Component({
  selector: "app-status-pill",
  templateUrl: "./status-pill.component.html"
})
export class StatusPillComponent implements OnInit {
  @Input() publishDate = ""
  @Input() archiveDate = ""

  iconUp = SvgHelper.path(SvgIcon.ICON_UP)
  iconArchive = SvgHelper.path(SvgIcon.ARCHIVE)

  constructor() {
  }

  ngOnInit(): void {
  }

  isPublished(): boolean {
    return !!this.publishDate
  }

  isArchived(): boolean {
    return !!this.archiveDate
  }

  isDraft(): boolean {
    return !this.publishDate
  }
}
