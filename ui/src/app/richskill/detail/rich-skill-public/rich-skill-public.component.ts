import {Component, Inject, LOCALE_ID, TemplateRef, ViewChild} from "@angular/core"
import { RichSkillService } from "../../service/rich-skill.service"
import { ActivatedRoute } from "@angular/router"
import {AbstractRichSkillDetailComponent} from "../AbstractRichSkillDetailComponent"
import {IDetailCardSectionData} from "../../../detail-card/section/section.component"
import {Title} from "@angular/platform-browser";

@Component({
  selector: "app-rich-skill-public",
  templateUrl: "./rich-skill-public.component.html"
})
export class RichSkillPublicComponent extends AbstractRichSkillDetailComponent {

  isOccupationsCollapsed = true

  // tslint:disable-next-line:no-any
  @ViewChild("occupationsTemplate", {read: TemplateRef}) occupationsTemplate!: TemplateRef<any>

  constructor(
    richSkillService: RichSkillService,
    route: ActivatedRoute,
    titleService: Title,
    @Inject(LOCALE_ID) locale: string
  ) {
    super(richSkillService, route, titleService, locale)
  }

  getCardFormat(): IDetailCardSectionData[] {
    return [
      {
        label: "Skill Statement",
        bodyString: this.richSkill?.skillStatement ?? "",
        showIfEmpty: false
      }, {
        label: "Category",
        bodyString: this.richSkill?.category ?? "",
        showIfEmpty: false
      }, {
        label: "Keywords",
        bodyString: this.richSkill?.keywords?.join("; ") ?? "",
        showIfEmpty: false
      },
      {
        label: "Standards",
        bodyString: this.richSkill?.standards?.map(({name}) => name)?.join("; ") ?? "",
        showIfEmpty: false
      }, {
        label: "Certifications",
        bodyString: this.richSkill?.certifications?.map(({name}) => name)?.join("; ") ?? "",
        showIfEmpty: false
      }, {
        label: "Occupations",
        bodyTemplate: this.occupationsTemplate,
        showIfEmpty: false
      }, {
        label: "Alignments",
        bodyString: this.richSkill?.sortedAlignments
          ?.map(alignment => {
            const framework = alignment.isPartOf ? `${alignment.isPartOf.name}: ` : ""
            return (alignment.id)
              ? `<p class="t-type-body">${framework}<a class="t-link" target="_blank" href="${alignment.id}">${alignment.skillName || alignment.id}</a></p>`
              : `${framework}${alignment.skillName}`
          })
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
        bodyString: this.formatAssociatedCollections(false),
        showIfEmpty: false
      },
    ]
  }
}
