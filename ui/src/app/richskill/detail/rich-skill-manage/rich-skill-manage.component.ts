import {Component, Inject, LOCALE_ID, TemplateRef, ViewChild} from "@angular/core"
import {RichSkillService} from "../../service/rich-skill.service"
import {ActivatedRoute} from "@angular/router"
import {AbstractRichSkillDetailComponent} from "../AbstractRichSkillDetailComponent"
import {IDetailCardSectionData} from "../../../detail-card/section/section.component"
import {Title} from "@angular/platform-browser"

@Component({
  selector: "app-rich-skill-manage",
  templateUrl: "./rich-skill-manage.component.html"
})
export class RichSkillManageComponent extends AbstractRichSkillDetailComponent {

  isOccupationsCollapsed = true

  // tslint:disable-next-line:no-any
  @ViewChild("occupationsTemplate", {read: TemplateRef}) occupationsTemplate!: TemplateRef<any>

  constructor(
    richSkillService: RichSkillService,
    route: ActivatedRoute,
    @Inject(LOCALE_ID) locale: string,
    titleService: Title,
  ) {
    super(richSkillService, route, titleService, locale)
  }

  getCardFormat(): IDetailCardSectionData[] {
    return [
      {
        label: "Skill Statement",
        bodyString: this.richSkill?.skillStatement ?? "",
        showIfEmpty: true
      }, {
        label: "Categories",
        bodyString: this.richSkill?.categories?.join("; ") ?? "",
        showIfEmpty: true
      }, {
        label: "Keywords",
        bodyString: this.richSkill?.keywords?.join("; ") ?? "",
        showIfEmpty: true
      },
      {
        label: "Standards",
        bodyString: this.richSkill?.standards?.map(standard => standard.skillName)?.join("; ") ?? "",
        showIfEmpty: true
      }, {
        label: "Certifications",
        bodyString: this.richSkill?.certifications?.map(alignment => alignment.name)?.join("; ") ?? "",
        showIfEmpty: true
      }, {
        label: "Occupations",
        bodyTemplate: this.occupationsTemplate,
        showIfEmpty: true
      },
      {
        label: "Employers",
        bodyString: this.richSkill?.employers?.map(employer => employer.name)?.join("; ") ?? "",
        showIfEmpty: true
      },
      {
        label: "Alignments",
        bodyString: this.richSkill?.sortedAlignments
          ?.map(alignment => {
            const framework = alignment.isPartOf ? `${alignment.isPartOf.name}: ` : ""
            return (alignment.id)
              ? `<p class="t-type-body">${framework}<a class="t-link" target="_blank" href="${alignment.id}">${alignment.skillName || alignment.id}</a></p>`
              : `${framework}${alignment.skillName}`
          })
          ?.join("") ?? "",
        showIfEmpty: true
      },
      {
        label: "Collections With This RSD",
        bodyString: this.formatAssociatedCollections(true),
        showIfEmpty: false
      },
    ]
  }
}
