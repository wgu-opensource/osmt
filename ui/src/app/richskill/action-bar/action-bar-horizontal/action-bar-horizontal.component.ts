import {Component, Inject, Input, LOCALE_ID} from "@angular/core"
import {Router} from "@angular/router"
import {RichSkillActionBar} from "../RichSkillActionBar";
import {RichSkillService} from "../../service/rich-skill.service";
import {ToastService} from "../../../toast/toast.service";

@Component({
  selector: "app-action-bar-horizontal",
  templateUrl: "./action-bar-horizontal.component.html"
})
export class ActionBarHorizontalComponent extends RichSkillActionBar {

  @Input() skillUuid = ""
  @Input() skillName = ""

  // This gets mapped to a visually hidden textarea so that javascript has a node to copy from
  href = ""
  jsonClipboard = ""

  constructor(
    router: Router,
    richSkillService: RichSkillService,
    toastService: ToastService,
    @Inject(LOCALE_ID) locale: string,
  ) {
    super(router, richSkillService, toastService, locale)
  }
}
