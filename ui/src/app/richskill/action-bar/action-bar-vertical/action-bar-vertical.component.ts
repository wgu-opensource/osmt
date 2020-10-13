import {Component, Inject, Input, LOCALE_ID, OnInit} from "@angular/core"
import {Router} from "@angular/router"
import {AppConfig} from "../../../app.config"
import {RichSkillService} from "../../service/rich-skill.service"
import {ToastService} from "../../../toast/toast.service"
import {RichSkillActionBarComponent} from "../rich-skill-action-bar.component"

@Component({
  selector: "app-action-bar",
  templateUrl: "./action-bar-vertical.component.html"
})
export class ActionBarVerticalComponent extends RichSkillActionBarComponent {

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
