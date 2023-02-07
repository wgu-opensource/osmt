import {Component, EventEmitter, Inject, Input, LOCALE_ID, Output} from "@angular/core"
import {Router} from "@angular/router"
import {ManageRichSkillActionBarComponent} from "../manage-rich-skill-action-bar.component"
import {RichSkillService} from "../../../../service/rich-skill.service"
import {ToastService} from "../../../../../toast/toast.service"
import {AuthService} from "../../../../../auth/auth-service";

@Component({
  selector: "app-manage-skill-action-bar-horizontal",
  templateUrl: "./manage-skill-action-bar-horizontal.component.html"
})
export class ManageSkillActionBarHorizontalComponent extends ManageRichSkillActionBarComponent {

  @Input() skillUuid = ""
  @Input() skillName = ""
  @Input() skillPublicUrl = ""
  @Input() archived = undefined
  @Input() published = undefined

  @Output() reloadSkill = new EventEmitter<void>()

  // This gets mapped to a visually hidden textarea so that javascript has a node to copy from
  href = ""
  jsonClipboard = ""

  constructor(
    router: Router,
    richSkillService: RichSkillService,
    toastService: ToastService,
    @Inject(LOCALE_ID) locale: string,
    authService: AuthService
  ) {
    super(router, richSkillService, toastService, locale, authService)
  }
}
