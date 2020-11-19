import {Component, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {AbstractControl, FormControl, FormGroup, Validators} from "@angular/forms"
import {AppConfig} from "../../app.config"
import {urlValidator} from "../../validators/url.validator"
import {SearchService} from "../search.service"
import {ApiAdvancedSearch} from "../../richskill/service/rich-skill-search.service"
import {INamedReference} from "../../richskill/ApiSkill";

@Component({
  selector: "app-advanced-search",
  templateUrl: "./advanced-search.component.html"
})
export class AdvancedSearchComponent implements OnInit {

  skillForm = new FormGroup(this.getFormDefinitions())

  iconSearch = SvgHelper.path(SvgIcon.SEARCH)
  constructor(
    private searchService: SearchService
  ) { }

  ngOnInit(): void {
  }

  getFormDefinitions(): {[key: string]: AbstractControl} {
    const fields = {
      skillName: new FormControl(""),
      author: new FormControl(""),
      skillStatement: new FormControl(""),
      category: new FormControl(""),
      keywords: new FormControl(""),
      standards: new FormControl(""),
      certifications: new FormControl(""),
      occupations: new FormControl(""),
      employers: new FormControl(""),
      alignments: new FormControl("", urlValidator),
      collectionName: new FormControl("")
    }
    if (AppConfig.settings.editableAuthor) {
      // @ts-ignore
      fields.author = new FormControl(AppConfig.settings.defaultAuthorValue, Validators.required)
    }
    return fields
  }

  handleSearchSkills(): void {
    this.searchService.advancedSkillSearch(this.collectFieldData())
  }

  handleSearchCollections(): void {
    this.searchService.advancedCollectionSearch(this.collectFieldData())
  }

  private collectFieldData(): ApiAdvancedSearch {
    const form = this.skillForm.value

    const skillName: string = form.skillName
    const author: string = form.author
    const skillStatement: string = form.skillStatement
    const category: string = form.category
    const keywords = this.tokenizeString(form.keywords)
    const standards = this.prepareNamedReferences(form.standards)
    const certifications = this.prepareNamedReferences(form.certifications)
    const occupations = this.prepareNamedReferences(form.certifications)
    const employers = this.prepareNamedReferences(form.certifications)
    const alignments = this.prepareNamedReferences(form.certifications)
    const collectionName = form.collectionName

    // if a property would be falsey, then completely omit it
    return {
      ...skillName && { skillName },
      ...author && { author },
      ...skillStatement && { skillStatement },
      ...category && { category},
      ...form.keywords && { keywords },
      ...form.standards && { standards },
      ...form.certifications && { certifications },
      ...form.occupations && { occupations },
      ...form.employers && { employers },
      ...form.alignments && { alignments },
      ...collectionName && { collectionName }
    }
  }

  prepareNamedReferences(value: string, token: string = ";"): INamedReference[] | undefined {
    return this.tokenizeString(value, token)?.map(v => ({name: v})) || undefined
  }

  // used for advance search to tokenize string fields
  tokenizeString(value: string, token: string = ";"): string[] | undefined {
    return value
        .split(token)
        .map(v => v.trim())
        .filter(v => v.length > 0)
      || undefined
  }

  showAuthor(): boolean {
    return AppConfig.settings.editableAuthor
  }

  getSemicolonHelpMessage(): string {
    return "Use a semicolon to separate multiple entries in a field."
  }

  getOccupationHelpMessage(): string {
    return "BLS or O*NET job codes."
  }
}
