import {Router} from "@angular/router"
import {RichSkillService} from "../../../service/rich-skill.service"
import {ToastService} from "../../../../toast/toast.service"
import {Component, EventEmitter, Inject, LOCALE_ID, OnInit} from "@angular/core"
import {AppConfig} from "../../../../app.config"
import {SvgHelper, SvgIcon} from "../../../../core/SvgHelper"
import {PublishStatus} from "../../../../PublishStatus"
import {ExtrasSelectedSkillsState} from "../../../../collection/add-skills-collection.component";
import {ApiSkillSummary} from "../../../ApiSkillSummary";

@Component({template: ""})
export abstract class ManageRichSkillActionBarComponent implements OnInit {

  private static MESSAGES = {
    SHARE: {
      SUCCESS: "This RSD will be available in the Search Hub soon",
      ERROR: {
        DEFAULT: "Unable to share to Search Hub"
      }
    },
    UNSHARE: {
      CONFIRM: "Are you sure you want to remove this RSD from the Search Hub? " +
               "This RSD will no longer be available in search results.",
      ERROR: {
        DEFAULT: "Unable to unshare from Search Hub"
      }
    }
  }

  abstract skillUuid: string
  abstract skillName: string
  abstract archived: string | undefined
  abstract published: string | undefined
  abstract isExternallyShared: boolean | undefined

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
  shareIcon: string = SvgHelper.path(SvgIcon.SHARE)
  unshareIcon: string = SvgHelper.path(SvgIcon.UNSHARE)
  externalIcon: string = SvgHelper.path(SvgIcon.EXTERNAL)

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
        this.jsonClipboard = json
      })
  }

  onAddToCollection(): void {
    const fakeSummary = new ApiSkillSummary({
      id: "",
      skillName: this.skillName,
      skillStatement: "",
      uuid: this.skillUuid,
      status: PublishStatus.Draft,
      category: "",
      keywords: [],
      occupations: []
    })
    this.router.navigate(["/collections/add-skills"], {
      state: {
        selectedSkills: [fakeSummary],
        totalCount: 1
      } as ExtrasSelectedSkillsState
    })

  }

  publishLinkText(): string {
    return this.isPublished() ? "View Published RSD" : "Publish"
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
      status: PublishStatus.Unarchived
    }).subscribe(() => {
      this.reloadSkill.emit()
      this.toastService.hideBlockingLoader()
    })
  }

  handlePublish(): void {
    if (!this.published) {
      if (confirm("Are you sure you want to publish this RSD?")) {
        this.toastService.showBlockingLoader()
        this.richSkillService.updateSkill(this.skillUuid, {
          status: PublishStatus.Published
        }).subscribe(() => {
          this.reloadSkill.emit()
          this.toastService.hideBlockingLoader()
        })
      }
    } else {
      const url = `skills/${this.skillUuid}`
      window.open(url, "_blank")
    }
  }

  handleExternalUrl(externalUrl: string): void {
    window.open(externalUrl, "_blank")
  }

  handleShareExternally(): void {
    if (!this.isExternallyShared) {
      this.toastService.showBlockingLoader()
      this.richSkillService.shareSkillExternally(this.skillUuid).subscribe(
        data => {
          this.reloadSkill.emit()
          this.toastService.hideBlockingLoader()
          alert(ManageRichSkillActionBarComponent.MESSAGES.SHARE.SUCCESS)
        },
        error => {
          this.toastService.hideBlockingLoader()
          alert(error?.error?.message ?? ManageRichSkillActionBarComponent.MESSAGES.SHARE.ERROR.DEFAULT)
        }
      )
    } else {
      const url = `skills/${this.skillUuid}`
      window.open(url, "_blank")
    }
  }

  handleUnshareExternally(): void {
    if (this.isExternallyShared) {
      if (confirm(ManageRichSkillActionBarComponent.MESSAGES.UNSHARE.CONFIRM)) {
        this.toastService.showBlockingLoader()
        this.richSkillService.unshareSkillExternally(this.skillUuid).subscribe(
          data => {
            this.reloadSkill.emit()
            this.toastService.hideBlockingLoader()
          },
          error => {
            this.toastService.hideBlockingLoader()
            alert(error?.error?.message ?? ManageRichSkillActionBarComponent.MESSAGES.UNSHARE.ERROR.DEFAULT)
          }
        )
      }
    } else {
      const url = `skills/${this.skillUuid}`
      window.open(url, "_blank")
    }
  }
}
