import {Component, Inject, Input, LOCALE_ID, OnInit} from "@angular/core"
import {Router} from "@angular/router"
import {AppConfig} from "../../../../../app.config"
import {RichSkillService} from "../../../../service/rich-skill.service"
import {ToastService} from "../../../../../toast/toast.service"
import {PublicRichSkillActionBarComponent} from "../public-rich-skill-action-bar.component"

@Component({
  selector: "app-public-skill-action-bar-vertical",
  templateUrl: "./public-skill-action-bar-vertical.component.html"
})
export class PublicSkillActionBarVerticalComponent extends PublicRichSkillActionBarComponent {

  @Input() skillUuid = ""
  @Input() skillName = ""
  @Input() skillUrl = ""

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
