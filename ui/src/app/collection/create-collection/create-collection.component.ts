import {Component, OnInit} from "@angular/core"
import {Location} from "@angular/common"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {AbstractControl, FormControl, FormGroup, Validators} from "@angular/forms"
import {AppConfig} from "../../app.config"
import {CollectionService} from "../service/collection.service"
import {PublishStatus} from "../../PublishStatus"
import {Router} from "@angular/router";

@Component({
  selector: "app-create-collection",
  templateUrl: "./create-collection.component.html"
})
export class CreateCollectionComponent implements OnInit {

  collectionForm = new FormGroup(this.getFormDefinitions())

  iconCollection = SvgHelper.path(SvgIcon.COLLECTION)

  constructor(
    private collectionService: CollectionService,
    private loc: Location,
    private router: Router
  ) { }

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
    this.collectionService.createCollection([{
      author: {
        name: formValues.author?.trim() ?? undefined
      },
      name: formValues.collectionName,
    }]).subscribe(collections => {
      this.router.navigate([`/collections/${collections[0].uuid}/manage`])
    })
  }

  handleCancel(): void {
    this.loc.back()
  }
}
