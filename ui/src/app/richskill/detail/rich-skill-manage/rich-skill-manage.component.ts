import {Component, Inject, LOCALE_ID} from "@angular/core"
import {RichSkillService} from "../../service/rich-skill.service"
import {ActivatedRoute} from "@angular/router"
import {AbstractRichSkillDetailComponent} from "../AbstractRichSkillDetailComponent"
import {IDetailCardSectionData} from "../../../detail-card/section/section.component"
import {OccupationsFormatter} from "../../../job-codes/Jobcode"
import {Title} from "@angular/platform-browser"

@Component({
  selector: "app-rich-skill-manage",
  templateUrl: "./rich-skill-manage.component.html"
})
export class RichSkillManageComponent extends AbstractRichSkillDetailComponent {

  constructor(
    richSkillService: RichSkillService,
    route: ActivatedRoute,
    @Inject(LOCALE_ID) locale: string,
    titleService: Title
  ) {
    super(richSkillService, route, locale)
    titleService.setTitle("Manage Rich Skill Descriptor")
  }

  getCardFormat(): IDetailCardSectionData[] {
    return [
      {
        label: "Skill Statement",
        bodyHtml: this.richSkill?.skillStatement ?? "",
        showIfEmpty: true
      }, {
        label: "Category",
        bodyHtml: this.richSkill?.category ?? "",
        showIfEmpty: true
      }, {
        label: "Keywords",
        bodyHtml: this.richSkill?.keywords?.join("; ") ?? "",
        showIfEmpty: true
      },
      {
        label: "Standards",
        bodyHtml: this.richSkill?.standards?.map(standard => standard.name)?.join("; ") ?? "",
        showIfEmpty: true
      }, {
        label: "Certifications",
        bodyHtml: this.richSkill?.certifications?.map(alignment => alignment.name)?.join("; ") ?? "",
        showIfEmpty: true
      }, {
        label: "Occupations",
        bodyHtml: new OccupationsFormatter(this.richSkill?.occupations ?? []).html(),
        showIfEmpty: true
      },
      {
        label: "Employers",
        bodyHtml: this.richSkill?.employers?.map(employer => employer.name)?.join("; ") ?? "",
        showIfEmpty: true
      },
      {
        label: "Alignment",
        bodyHtml: this.richSkill?.alignments
          ?.map(alignment => {
            return (alignment.id)
              ? `<a class="t-link" target="_blank" href="${alignment.id}">${alignment.name || alignment.id}</a>`
              : `${alignment.name}`
          })
          ?.join("") ?? "",
        showIfEmpty: true
      },
      {
        label: "Collections With This RSD",
        bodyHtml: this.formatAssociatedCollections(),
        showIfEmpty: false
      },
    ]
  }
}
