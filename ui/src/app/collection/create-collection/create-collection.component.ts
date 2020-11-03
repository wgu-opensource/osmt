import {Component, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {AbstractControl, FormControl, FormGroup, Validators} from "@angular/forms";
import {urlValidator} from "../../validators/url.validator";
import {AppConfig} from "../../app.config";

@Component({
  selector: "app-create-collection",
  templateUrl: "./create-collection.component.html"
})
export class CreateCollectionComponent implements OnInit {

  skillForm = new FormGroup(this.getFormDefinitions())

  iconCollection = SvgHelper.path(SvgIcon.COLLECTION)

  constructor() { }

  ngOnInit(): void {
    if (this.isAuthorEditable()) {
      this.skillForm.controls.author.setValue(AppConfig.settings.defaultAuthorValue)
    }
  }

  getFormDefinitions(): {[key: string]: AbstractControl} {
    const fields = {
      collectionName: new FormControl(""),
    }
    if (this.isAuthorEditable()) {
      // @ts-ignore
      fields.author = new FormControl(AppConfig.settings.defaultAuthorValue, Validators.required)
    }
    return fields
  }

  isAuthorEditable(): boolean {
    return AppConfig.settings.editableAuthor
  }

  handleSaved(): void {
    console.log("Save clicked")
  }

  handleCancel(): void {
    console.log("Cancel clicked")
  }
}
