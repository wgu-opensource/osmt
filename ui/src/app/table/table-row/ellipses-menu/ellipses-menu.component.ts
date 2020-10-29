import {Component, ElementRef, EventEmitter, HostListener, Input, OnInit, Output, Renderer2} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../../core/SvgHelper"
import {IApiSkillSummary} from "../../../richskill/ApiSkillSummary"
import {PublishStatus} from "../../../PublishStatus"

export interface SkillActions {
  handleAddToCollection: (skill: IApiSkillSummary | null) => void
  handlePublish: (skill: IApiSkillSummary | null) => void
  handleArchive: (skill: IApiSkillSummary | null) => void
  handleUnarchive: (skill: IApiSkillSummary | null) => void
}

@Component({
  selector: "app-ellipses-menu",
  templateUrl: "./ellipses-menu.component.html"
})
export class EllipsesMenuComponent implements OnInit {

  @Input() skill: IApiSkillSummary | null = null
  @Input() id = ""
  @Input() actions: SkillActions = {
    handlePublish: s => {},
    handleArchive: s => {},
    handleUnarchive: s => {},
    handleAddToCollection: s => {}
  }

  isOpen = false

  moreIcon = SvgHelper.path(SvgIcon.MORE)

  // Used by the host listener by prepending with the id to identify
  // when a click happens outside this component's instance
  clickHandle = "click-handle"

  constructor(private elementRef: ElementRef) { }

  @HostListener("document:click", ["$event.target"])
  handleContextClick(event: HTMLElement): void {
    if (event.getAttribute("id") !== this.prependId(this.clickHandle)) {
      this.isOpen = false // The user clicked away from this instance
    }
  }

  ngOnInit(): void {}

  prependId(suffix: string): string {
    return `${this.id}-${suffix}`
  }

  handleClick(): boolean {
    return this.isOpen = !this.isOpen
  }

  isPublished(): boolean {
    return this.skill?.status === PublishStatus.Published
  }

  isArchived(): boolean {
    return this.skill?.status === PublishStatus.Archived
  }
}
