import {Component, Input, OnInit} from "@angular/core";
import {PublishStatus} from "../PublishStatus";
import {SvgHelper, SvgIcon} from "./SvgHelper";

@Component({
  selector: "app-status-pill",
  templateUrl: "./status-pill.component.html"
})
export class StatusPillComponent implements OnInit {
  @Input() status: PublishStatus = PublishStatus.Unpublished
  @Input() publishDate = ""
  @Input() archiveDate = ""

  iconUp = SvgHelper.path(SvgIcon.ICON_UP)
  iconArchive = SvgHelper.path(SvgIcon.ARCHIVE)

  constructor() {
  }

  ngOnInit(): void {
  }

  isPublished(): boolean {
    return this.status !== PublishStatus.Unpublished
  }

  isArchived(): boolean {
    return this.status === PublishStatus.Archived
  }

  isDraft(): boolean {
    return this.status === PublishStatus.Unpublished
  }
}
