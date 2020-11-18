import {Component, Input, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "./SvgHelper"
import {PublishStatus} from "../PublishStatus";

@Component({
  selector: "app-status-bar",
  templateUrl: "./status-bar.component.html"
})
export class StatusBarComponent implements OnInit {

  @Input() status: PublishStatus = PublishStatus.Unarchived
  @Input() publishDate = ""
  @Input() archiveDate = ""
  @Input() showDates = true

  iconUp = SvgHelper.path(SvgIcon.ICON_UP)
  archiveIcon = SvgHelper.path(SvgIcon.ARCHIVE)

  constructor() { }

  ngOnInit(): void {
  }
}
