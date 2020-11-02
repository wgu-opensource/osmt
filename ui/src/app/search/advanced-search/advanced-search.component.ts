import {Component, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {AbstractControl, FormControl, FormGroup, Validators} from "@angular/forms"
import {AppConfig} from "../../app.config"
import {urlValidator} from "../../validators/url.validator"
import {SearchService} from "../search.service"
import {ApiAdvancedSearch, IAdvancedSearch} from "../../richskill/service/rich-skill-search.service"

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
    console.log("Searching skills...")
    this.searchService.advancedSkillSearch(this.collectFieldData())
  }

  handleSearchCollections(): void {
    console.log("Searching collections...")
  }

  private collectFieldData(): ApiAdvancedSearch {
    const {
      skillName,
      author,
      skillStatement,
      category,
      keywords,
      standards,
      certifications,
      occupations,
      employers,
      alignments,
      collectionName
    } = this.skillForm.value

    return ApiAdvancedSearch.factory({
      skillName: skillName || undefined,
      author: author || undefined,
      skillStatement: skillStatement || undefined,
      category: category || undefined,
      keywords: keywords || undefined,
      standards: standards || undefined,
      certifications: certifications || undefined,
      occupations: occupations || undefined,
      employers: employers || undefined,
      alignments: alignments || undefined,
      collectionName: collectionName || undefined
    })
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
