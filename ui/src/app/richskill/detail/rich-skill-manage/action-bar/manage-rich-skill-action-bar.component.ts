import {Router} from "@angular/router"
import {RichSkillService} from "../../../service/rich-skill.service"
import {ToastService} from "../../../../toast/toast.service"
import {Component, EventEmitter, Inject, LOCALE_ID, OnInit} from "@angular/core"
import {AppConfig} from "../../../../app.config"
import {SvgHelper, SvgIcon} from "../../../../core/SvgHelper"
import {PublishStatus} from "../../../../PublishStatus"

@Component({template: ""})
export abstract class ManageRichSkillActionBarComponent implements OnInit {

  abstract skillUuid: string
  abstract skillName: string
  abstract archived: string | undefined
  abstract published: string | undefined

  abstract reloadSkill: EventEmitter<void>

  // Used in invisible labels to house the data to be added to clipboard
  abstract href: string
  abstract jsonClipboard: string

  // icons
  editIcon: string = SvgHelper.path(SvgIcon.EDIT)
  duplicateIcon: string = SvgHelper.path(SvgIcon.DUPLICATE)
  publishIcon: string = SvgHelper.path(SvgIcon.PUBLISH)
  archiveIcon: string = SvgHelper.path(SvgIcon.ARCHIVE)
  dismissIcon: string = SvgHelper.path(SvgIcon.DISMISS)

  constructor(
    protected router: Router,
    protected richSkillService: RichSkillService,
    protected toastService: ToastService,
    @Inject(LOCALE_ID) protected locale: string
  ) {
  }

  ngOnInit(): void {
    this.href = `${AppConfig.settings.baseApiUrl}${this.router.url}`
    this.richSkillService.getSkillJsonByUuid(this.skillUuid)
      .subscribe( (json: string) => {
        console.log("got response: " + json )
        this.jsonClipboard = json
      })
  }

  onAddToCollection(): void {

  }

  publishLinkText(): string {
    return this.isPublished() ? "View Published Skill" : "Publish"
  }

  publishLinkDestination(): string {
    return this.isPublished() ? "../" : "" // TODO Implement
  }

  private isPublished(): boolean {
    return !!this.published
  }

  handleArchive(): void {
    this.toastService.showBlockingLoader()
    this.richSkillService.updateSkill(this.skillUuid, {
      status: PublishStatus.Archived
    }).subscribe(() => {
      this.reloadSkill.emit()
      this.toastService.hideBlockingLoader()
    })
  }

  handleUnarchive(): void {
    this.toastService.showBlockingLoader()
    this.richSkillService.updateSkill(this.skillUuid, {
      status: PublishStatus.Published
    }).subscribe(() => {
      this.reloadSkill.emit()
      this.toastService.hideBlockingLoader()
    })
  }

  handlePublish(): void {
    if (!this.published) {
      this.toastService.showBlockingLoader()
      this.richSkillService.updateSkill(this.skillUuid, {
        status: PublishStatus.Published
      }).subscribe(() => {
        this.reloadSkill.emit()
        this.toastService.hideBlockingLoader()
      })
    } else {
      this.router.navigate([`skills/${this.skillUuid}`])
    }
  }
}
