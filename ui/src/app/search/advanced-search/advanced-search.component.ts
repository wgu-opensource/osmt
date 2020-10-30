import {Component, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper";
import {AbstractControl, FormControl, FormGroup, Validators} from "@angular/forms";
import {AppConfig} from "../../app.config";
import {urlValidator} from "../../validators/url.validator";

@Component({
  selector: "app-advanced-search",
  templateUrl: "./advanced-search.component.html"
})
export class AdvancedSearchComponent implements OnInit {

  skillForm = new FormGroup(this.getFormDefinitions())

  iconSearch = SvgHelper.path(SvgIcon.SEARCH)
  constructor() { }

  ngOnInit(): void {
  }

  getFormDefinitions(): {[key: string]: AbstractControl} {
    const fields = {
      skillName: new FormControl("", Validators.required),
      skillStatement: new FormControl("", Validators.required),
      category: new FormControl(""),
      keywords: new FormControl(""),
      standards: new FormControl(""),
      collections: new FormControl(""),
      certifications: new FormControl(""),
      occupations: new FormControl(""),
      employers: new FormControl(""),
      alignmentText: new FormControl(""),
      alignmentUrl: new FormControl("", urlValidator),
    }
    if (AppConfig.settings.editableAuthor) {
      // @ts-ignore
      fields.author = new FormControl(AppConfig.settings.defaultAuthorValue, Validators.required)
    }
    return fields
  }

  handleSearchSkills(): void {
    console.log("Searching skills...")
  }

  handleSearchCollections(): void {
    console.log("Searching collections...")
  }
}
