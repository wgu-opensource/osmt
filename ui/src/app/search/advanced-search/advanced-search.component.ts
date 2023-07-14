import {Component, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {AbstractControl, FormControl, FormGroup, Validators} from "@angular/forms"
import {AppConfig} from "../../app.config"
import {urlValidator} from "../../validators/url.validator"
import {SearchService} from "../search.service"
import {ApiAdvancedSearch} from "../../richskill/service/rich-skill-search.service"
import {ApiNamedReference, IAlignment, INamedReference, KeywordType} from "../../richskill/ApiSkill"
import {Title} from "@angular/platform-browser";
import {Whitelabelled} from "../../../whitelabel";
import { IJobCode } from "../../metadata/job-code/Jobcode";

@Component({
  selector: "app-advanced-search",
  templateUrl: "./advanced-search.component.html"
})
export class AdvancedSearchComponent extends Whitelabelled implements OnInit {

  keywordType = KeywordType
  skillForm = new FormGroup(this.getFormDefinitions())

  iconSearch = SvgHelper.path(SvgIcon.SEARCH)
  constructor(
    private searchService: SearchService,
    protected titleService: Title
  ) {
    super()
  }

  ngOnInit(): void {
    this.titleService.setTitle(`Advanced Search | ${this.whitelabel.toolName}`)
  }

  getFormDefinitions(): {[key: string]: AbstractControl} {
    const fields = {
      name: new FormControl(""),
      author: new FormControl(null),
      skillStatement: new FormControl(""),
      category: new FormControl(null),
      keywords: new FormControl(null),
      standards: new FormControl(null),
      certifications: new FormControl(null),
      occupations: new FormControl(null),
      employers: new FormControl(null),
      alignments: new FormControl(null, urlValidator),
      collectionName: new FormControl(""),
    }
    return fields
  }

  handleSearchSkills(): void {
    this.searchService.advancedSkillSearch(this.collectFieldData())
  }

  handleSearchCollections(): void {
    this.searchService.advancedCollectionSearch(this.collectFieldData(true))
  }

  private collectFieldData(isSearchingCollections: boolean = false): ApiAdvancedSearch {
    const form = this.skillForm.value

    const name: string = form.name
    const author: string = form.author
    const skillStatement: string = form.skillStatement
    const category: string = form.category
    const keywords = (form.keywords && form.keywords.length > 0)
      ? form.keywords.map((v: INamedReference) => v) : undefined
    // Necessary because of type mismatch between ApiSkill(IAlignment[]) & ApiAdvancedSearch(INamedReference[])
    const standards = (form.standards && form.standards.length > 0)
      ? form.standards.map((v: IAlignment) => new ApiNamedReference({ name: v.skillName })) : undefined
    const certifications = (form.certifications && form.certifications.length > 0)
      ? form.certifications.map((v: INamedReference) => v) : undefined
    const occupations = (form.occupations && form.occupations.length > 0)
      ? form.occupations.map((v: IJobCode) => v.code) : undefined
    const employers = (form.employers && form.employers.length > 0)
      ? form.employers.map((v: INamedReference) => v) : undefined
    // Necessary because of type mismatch between ApiSkill(IAlignment[]) & ApiAdvancedSearch(INamedReference[])
    const alignments = (form.alignments && form.alignments.length > 0)
      ? form.alignments.map((v: IAlignment) => new ApiNamedReference({ name: v.skillName})) : undefined
    const collectionName = form.collectionName

    // if a property would be falsey, then completely omit it
    return {
      ...isSearchingCollections && { collectionName: name },
      ...!isSearchingCollections && { skillName: name },
      ...author && { author },
      ...skillStatement && { skillStatement },
      ...category && { category },
      ...keywords && { keywords },
      ...standards && { standards },
      ...certifications && { certifications },
      ...occupations && { occupations },
      ...employers && { employers },
      ...alignments && { alignments },
      ...collectionName && { collectionName }
    }
  }

  showAuthor(): boolean {
    return AppConfig.settings.editableAuthor
  }

  getOccupationHelpMessage(): string {
    return "BLS or O*NET job names or codes"
  }
}
