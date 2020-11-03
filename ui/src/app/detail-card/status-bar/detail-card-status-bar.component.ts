import {Component, Input, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {PublishStatus} from "../../PublishStatus";

@Component({
  selector: "app-detail-card-status-bar",
  templateUrl: "./detail-card-status-bar.component.html"
})
export class DetailCardStatusBarComponent implements OnInit {

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
