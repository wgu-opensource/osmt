import { Component, Inject, LOCALE_ID } from "@angular/core"
import { RichSkillService } from "../../service/rich-skill.service"
import { ActivatedRoute } from "@angular/router"
import {AbstractRichSkillDetailComponent} from "../AbstractRichSkillDetailComponent"
import {OccupationsFormatter} from "../../../job-codes/Jobcode";
import {IDetailCardSectionData} from "../../../detail-card/section/section.component";

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

  getCardFormat(): IDetailCardSectionData[] {
    return [
      {
        label: "Skill Statement",
        bodyHtml: this.richSkill?.skillStatement ?? "",
        showIfEmpty: false
      }, {
        label: "Category",
        bodyHtml: this.richSkill?.category ?? "",
        showIfEmpty: false
      }, {
        label: "Keywords",
        bodyHtml: this.richSkill?.keywords?.join("; ") ?? "",
        showIfEmpty: false
      },
      {
        label: "Standards",
        bodyHtml: this.richSkill?.standards?.map(standard => standard.name)?.join("\n") ?? "",
        showIfEmpty: false
      }, {
        label: "Certifications",
        bodyHtml: this.richSkill?.certifications?.map(alignment => alignment.name)?.join("\n") ?? "",
        showIfEmpty: false
      }, {
        label: "Occupations",
        bodyHtml: new OccupationsFormatter(this.richSkill?.occupations ?? []).html(),
        showIfEmpty: false
      }, {
        label: "Alignment",
        bodyHtml: this.richSkill?.alignments
          ?.map(alignment => `<a class="t-link" target="_blank" href="${alignment.id}">${alignment.name}</a>`)
          ?.join("") ?? "",
        showIfEmpty: false
      },
      // {
      //   label: "Employers",
      //   bodyHtml: this.richSkill?.employers?.map(employer => employer.name)?.join("; ") ?? "",
      //   showIfEmpty: false
      // },
      {
        label: "Collections With This RSD",
        bodyHtml: this.formatAssociatedCollections(),
        showIfEmpty: false
      },
    ]
  }
}
