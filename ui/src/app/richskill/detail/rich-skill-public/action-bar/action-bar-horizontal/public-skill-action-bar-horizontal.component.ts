import {Component, Inject, Input, LOCALE_ID} from "@angular/core"
import {Router} from "@angular/router"
import {PublicRichSkillActionBarComponent} from "../public-rich-skill-action-bar.component";
import {RichSkillService} from "../../../../service/rich-skill.service";
import {ToastService} from "../../../../../toast/toast.service";

@Component({
  selector: "app-public-skill-action-bar-horizontal",
  templateUrl: "./public-skill-action-bar-horizontal.component.html"
})
export class PublicSkillActionBarHorizontalComponent extends PublicRichSkillActionBarComponent {

  constructor(
    router: Router,
    richSkillService: RichSkillService,
    toastService: ToastService,
    @Inject(LOCALE_ID) locale: string
  ) {
    super(router, richSkillService, toastService, locale)
  }
}
