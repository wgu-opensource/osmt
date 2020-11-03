import {Component, Input, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "./SvgHelper"

@Component({
  selector: "app-accordian",
  templateUrl: "./accordian.component.html"
})
export class AccordianComponent implements OnInit {

  @Input() buttonText = ""
  @Input() keywords = ""
  @Input() detailedOccupations = ""

  isExpanded = false

  dismissIcon = SvgHelper.path(SvgIcon.DISMISS)
  addIcon = SvgHelper.path(SvgIcon.ADD)

  constructor() { }

  ngOnInit(): void {
  }

}
