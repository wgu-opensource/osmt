import {Component, OnInit} from "@angular/core"
import {Location} from "@angular/common"
import {SvgHelper, SvgIcon} from "../../core/SvgHelper"
import {AbstractControl, FormControl, FormGroup, Validators} from "@angular/forms"
import {AppConfig} from "../../app.config"
import {CollectionService} from "../service/collection.service"
import {ActivatedRoute, Router} from "@angular/router"
import {ApiCollection, ICollectionUpdate} from "../ApiCollection"
import {Observable} from "rxjs"
import {ToastService} from "../../toast/toast.service"
import {Title} from "@angular/platform-browser"
import {ApiNamedReference} from "../../richskill/ApiSkill"
import {HasFormGroup} from "../../core/abstract-form.component"
import {Whitelabelled} from "../../../whitelabel";

@Component({
  selector: "app-create-collection",
  templateUrl: "./collection-form.component.html"
})
export class CollectionFormComponent extends Whitelabelled implements OnInit, HasFormGroup {

  collectionForm = new FormGroup(this.getFormDefinitions())
  collectionUuid: string | null = null
  existingCollection?: ApiCollection

  collectionLoaded?: Observable<ApiCollection>
  collectionSaved?: Observable<ApiCollection>

  iconCollection = SvgHelper.path(SvgIcon.COLLECTION)

  get nameLabel(): string {
    return this.collectionUuid ? "Collection Name" : "New Collection Name"
  }

  constructor(
    protected collectionService: CollectionService,
    protected loc: Location,
    protected router: Router,
    protected route: ActivatedRoute,
    protected toastService: ToastService,
    protected titleService: Title
  ) {
    super()
  }

  formGroup(): FormGroup {
    return this.collectionForm
  }

  ngOnInit(): void {
    this.collectionUuid = this.route.snapshot.paramMap.get("uuid")

    if (this.collectionUuid) {
      this.collectionLoaded = this.collectionService.getCollectionByUUID(this.collectionUuid)
      this.collectionLoaded.subscribe(collection => {
        this.setCollection(collection)
      })
    }

    this.collectionForm.controls.author.setValue(AppConfig.settings.defaultAuthorValue)

    this.titleService.setTitle(`${this.pageTitle()} | ${this.whitelabel.toolName}`)
  }

  pageTitle(): string {
    return `${this.collectionUuid !== null ? "Edit" : "Create"} Collection`
  }

  getFormDefinitions(): { [key: string]: AbstractControl } {
    const fields = {
      collectionName: new FormControl("", Validators.required),
      description: new FormControl(""),
      author:  new FormControl(AppConfig.settings.defaultAuthorValue, Validators.required)
    }
    return fields
  }

  namedReferenceForString(value: string): ApiNamedReference | undefined {
    const str = value.trim()
    if (str.length < 1) {
      return undefined
    } else if (str.indexOf("://") !== -1) {
      return new ApiNamedReference({id: str})
    } else {
      return new ApiNamedReference({name: str})
    }
  }

  setCollection(collection: ApiCollection): void {
    this.existingCollection = collection
    const fields = {
      collectionName: collection.name,
      description: collection.description ?? "",
      author: collection.author
    }
    this.collectionForm.setValue(fields)
  }

  isAuthorEditable(): boolean {
    return AppConfig.settings.editableAuthor
  }

  updateObject(): ICollectionUpdate {
    const formValues = this.collectionForm.value
    return {
      name: formValues.collectionName,
      description: formValues.description,
      author: formValues.author
    }
  }

  onSubmit(): void {
    const updateObject = this.updateObject()

    if (this.collectionUuid) {
      this.collectionSaved = this.collectionService.updateCollection(this.collectionUuid, updateObject)
    } else {
      this.collectionSaved = this.collectionService.createCollection(updateObject)
    }

    if (this.collectionSaved) {
      this.collectionSaved.subscribe(collection => {
        this.collectionForm.markAsPristine()
        this.router.navigate([`/collections/${collection.uuid}/manage`])
      })
    }
  }

  handleCancel(): void {
    this.loc.back()
  }

  focusFormField(fieldName: string): boolean {
    const containerId = `formfield-container-${fieldName}`
    const el = document.getElementById(containerId)
    if (el) {
      el.scrollIntoView()

      const fieldId = `formfield-${fieldName}`
      const field = document.getElementById(fieldId)
      if (field) {
        field.focus()
      }
      return true
    }
    return false
  }

  scrollToTop(): boolean {
    this.focusFormField("collectionName")
    return false
  }

  scrollToTopHidden(): boolean {
    const form = this.collectionForm
    if (!form) {
      return false
    }

    return !(form.touched && !form.pristine && form.valid)
  }

  handleClickMissingFields(): boolean {
    this.collectionForm.markAllAsTouched()
    for (const fieldName of ["collectionName", "description", "author"]) {
      const control = this.collectionForm.controls[fieldName]
      if (control && !control.valid) {
        return this.focusFormField(fieldName)
      }
    }
    return false
  }
}
