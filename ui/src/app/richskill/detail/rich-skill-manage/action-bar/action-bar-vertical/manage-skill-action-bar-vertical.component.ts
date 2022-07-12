import {Component, EventEmitter, Inject, Input, LOCALE_ID, OnInit, Output} from "@angular/core"
import {Router} from "@angular/router"
import {AppConfig} from "../../../../../app.config"
import {RichSkillService} from "../../../../service/rich-skill.service"
import {ToastService} from "../../../../../toast/toast.service"
import {ManageRichSkillActionBarComponent} from "../manage-rich-skill-action-bar.component"
import {SvgHelper} from "../../../../../core/SvgHelper"
import {AuthService} from "../../../../../auth/auth-service";

@Component({
  selector: "app-manage-skill-action-bar-vertical",
  templateUrl: "./manage-skill-action-bar-vertical.component.html"
})
export class ManageSkillActionBarVerticalComponent extends ManageRichSkillActionBarComponent {

  @Input() skillUuid = ""
  @Input() skillName = ""
  @Input() archived = undefined
  @Input() published = undefined

  @Output() reloadSkill = new EventEmitter<void>()

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
