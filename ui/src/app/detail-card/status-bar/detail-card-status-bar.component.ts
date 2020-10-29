import {Component, Input, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"

@Component({
  selector: "app-detail-card-status-bar",
  templateUrl: "./detail-card-status-bar.component.html"
})
export class DetailCardStatusBarComponent implements OnInit {

  @Input() published = ""
  @Input() archived = ""

  iconUp = SvgHelper.path(SvgIcon.ICON_UP)
  archiveIcon = SvgHelper.path(SvgIcon.ARCHIVE)

  constructor() { }

  ngOnInit(): void {
  }

  isPublished(): boolean {
    return !!this.published
  }

  isArchived(): boolean {
    return !!this.archived
  }
}
