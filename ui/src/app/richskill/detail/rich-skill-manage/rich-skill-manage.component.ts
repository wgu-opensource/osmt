import {Component, Inject, LOCALE_ID, OnInit} from "@angular/core"
import {RichSkillService} from "../../service/rich-skill.service"
import {ActivatedRoute} from "@angular/router"
import {AbstractRichSkillDetailComponent} from "../AbstractRichSkillDetailComponent"

@Component({
  selector: "app-rich-skill-manage",
  templateUrl: "./rich-skill-manage.component.html"
})
export class RichSkillManageComponent extends AbstractRichSkillDetailComponent {

  constructor(
    richSkillService: RichSkillService,
    route: ActivatedRoute,
    @Inject(LOCALE_ID) locale: string
  ) {
    super(richSkillService, route, locale)
  }
}
