import {Component, OnInit} from "@angular/core"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {AbstractControl, FormControl, FormGroup, Validators} from "@angular/forms"
import {AppConfig} from "../../app.config"
import {CollectionService} from "../service/collection.service"
import {PublishStatus} from "../../PublishStatus"

@Component({
  selector: "app-create-collection",
  templateUrl: "./create-collection.component.html"
})
export class CreateCollectionComponent implements OnInit {

  collectionForm = new FormGroup(this.getFormDefinitions())

  iconCollection = SvgHelper.path(SvgIcon.COLLECTION)

  constructor(private collectionService: CollectionService) { }

  ngOnInit(): void {
    if (this.isAuthorEditable()) {
      this.collectionForm.controls.author.setValue(AppConfig.settings.defaultAuthorValue)
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
    const formValues = this.collectionForm.value
    console.log("Save clicked")
    this.collectionService.createCollection([{
      author: {
        name: formValues.author?.trim() ?? undefined
      },
      name: formValues.collectionName,
      skills: {
        add: [],
        remove: []
      },
      status: PublishStatus.Unpublished
    }])
      .subscribe(collection => {
        console.log(JSON.stringify(collection))
      })
  }

  handleCancel(): void {
    console.log("Cancel clicked")
  }
}
