import {Component, Inject, LOCALE_ID, TemplateRef, ViewChild} from "@angular/core"
import { RichSkillService } from "../../service/rich-skill.service"
import { ActivatedRoute } from "@angular/router"
import {AbstractRichSkillDetailComponent} from "../AbstractRichSkillDetailComponent"
import {IDetailCardSectionData} from "../../../detail-card/section/section.component"

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
    @Inject(LOCALE_ID) locale: string
  ) {
    super(richSkillService, route, locale)
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
        label: "Alignment",
        bodyString: this.richSkill?.alignments
          ?.map(alignment => {
            return (alignment.id)
              ? `<a class="t-link" target="_blank" href="${alignment.id}">${alignment.name || alignment.id}</a>`
              : `${alignment.name}`
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
