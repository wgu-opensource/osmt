import {Component, Inject, Input, LOCALE_ID} from "@angular/core"
import {Router} from "@angular/router"
import {RichSkillActionBarComponent} from "../rich-skill-action-bar.component";
import {RichSkillService} from "../../service/rich-skill.service";
import {ToastService} from "../../../toast/toast.service";

@Component({
  selector: "app-action-bar-horizontal",
  templateUrl: "./action-bar-horizontal.component.html"
})
export class ActionBarHorizontalComponent extends RichSkillActionBarComponent {

  @Input() skillUuid = ""
  @Input() skillName = ""

  // This gets mapped to a visually hidden textarea so that javascript has a node to copy from
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
