import {Component, Input, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "./SvgHelper"
import {PublishStatus} from "../PublishStatus";

@Component({
  selector: "app-status-bar",
  templateUrl: "./status-bar.component.html"
})
export class StatusBarComponent implements OnInit {

  @Input() status: PublishStatus = PublishStatus.Unpublished
  @Input() publishDate = ""
  @Input() archiveDate = ""

  iconUp = SvgHelper.path(SvgIcon.ICON_UP)
  archiveIcon = SvgHelper.path(SvgIcon.ARCHIVE)

  constructor() { }

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
