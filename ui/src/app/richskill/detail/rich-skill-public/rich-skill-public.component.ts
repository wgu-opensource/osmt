import { Component, Inject, InjectionToken, LOCALE_ID, OnInit } from "@angular/core"
import {ApiNamedReference, INamedReference, ApiSkill} from "../../ApiSkill"
import { RichSkillService } from "../../service/rich-skill.service"
import { ActivatedRoute } from "@angular/router"
import {JobCodeBreakout, IJobCode, OccupationsFormatter} from "../../../job-codes/Jobcode"
import { formatDate } from "@angular/common"
import {IDetailCardSectionData} from "../../../detail-card/section/section.component"
import {AbstractRichSkillDetailComponent} from "../AbstractRichSkillDetailComponent";

@Component({
  selector: "app-rich-skill-public",
  templateUrl: "./rich-skill-public.component.html"
})
export class RichSkillPublicComponent extends AbstractRichSkillDetailComponent {
  constructor(
    richSkillService: RichSkillService,
    route: ActivatedRoute,
    @Inject(LOCALE_ID) locale: string
  ) {
    super(richSkillService, route, locale)
  }


}
