import {Component, Inject, Input, LOCALE_ID, OnInit} from "@angular/core"
import {Router} from "@angular/router"
import {AppConfig} from "../../../../../app.config"
import {RichSkillService} from "../../../../service/rich-skill.service"
import {ToastService} from "../../../../../toast/toast.service"
import {ManageRichSkillActionBarComponent} from "../manage-rich-skill-action-bar.component"
import {SvgHelper} from "../../../../../core/SvgHelper"

@Component({
  selector: "app-manage-skill-action-bar-vertical",
  templateUrl: "./manage-skill-action-bar-vertical.component.html"
})
export class ManageSkillActionBarVerticalComponent extends ManageRichSkillActionBarComponent {

  @Input() skillUuid = ""
  @Input() skillName = ""

  href = ""
  jsonClipboard = ""

  constructor(
    router: Router,
    richSkillService: RichSkillService,
    toastService: ToastService,
    @Inject(LOCALE_ID) locale: string
  ) {
    super(router, richSkillService, toastService, locale)
  }
}
