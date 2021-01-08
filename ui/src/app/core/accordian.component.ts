import {Component, Input, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "./SvgHelper"
import {Observable} from "rxjs"

@Component({
  selector: "app-accordian",
  templateUrl: "./accordian.component.html"
})
export class AccordianComponent implements OnInit {
  @Input() isExpanded = false

  @Input() closedLabel = "View All"
  @Input() openLabel = "View Less"

  @Input() collapseListener = new Observable<void>()

  get triggerLabel(): string {
    if (this.isExpanded) {
      return this.openLabel
    }
    return this.closedLabel
  }


  dismissIcon = SvgHelper.path(SvgIcon.DISMISS)
  addIcon = SvgHelper.path(SvgIcon.ADD)

  constructor() { }

  ngOnInit(): void {
    this.collapseListener.subscribe(() => this.isExpanded = false)
  }


  handleClickTrigger(): boolean {
    this.toggle()
    return false
  }

  toggle(): void {
    this.isExpanded = !this.isExpanded
  }
}
